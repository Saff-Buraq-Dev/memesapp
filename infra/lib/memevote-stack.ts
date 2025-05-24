import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as cloudfront from 'aws-cdk-lib/aws-cloudfront';
import * as origins from 'aws-cdk-lib/aws-cloudfront-origins';
import * as route53 from 'aws-cdk-lib/aws-route53';
import * as targets from 'aws-cdk-lib/aws-route53-targets';
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecsp from 'aws-cdk-lib/aws-ecs-patterns';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as logs from 'aws-cdk-lib/aws-logs';
import * as s3deploy from 'aws-cdk-lib/aws-s3-deployment';
import * as ecrAssets from 'aws-cdk-lib/aws-ecr-assets';
import * as iam from 'aws-cdk-lib/aws-iam';

// Configuration interface
export interface MemeVoteConfig {
  projectName: string;
  environment: string;
  domainName: string;
  frontendSubdomain: string;
  backendSubdomain: string;
  dbName: string;
  dbUsername: string;
  ecsTaskCpu: number;
  ecsTaskMemory: number;
  frontendCachePolicy: {
    defaultTtl: cdk.Duration;
    maxTtl: cdk.Duration;
    minTtl: cdk.Duration;
  };
}

// Stack props interface
export interface MemeVoteStackProps extends cdk.StackProps {
  config: MemeVoteConfig;
}

export class MemeVoteStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: MemeVoteStackProps) {
    super(scope, id, props);

    const { config } = props;
    const isProd = config.environment === 'prod' || config.environment === 'production';

    // Use the default VPC for simplicity
    const vpc = ec2.Vpc.fromLookup(this, 'DefaultVPC', {
      isDefault: true,
    });

    // Create a security group for the database
    const dbSecurityGroup = new ec2.SecurityGroup(this, 'DatabaseSecurityGroup', {
      vpc,
      description: 'Security group for the MemeVote database',
      allowAllOutbound: true,
    });

    // Create a security group for the ECS service
    const ecsSecurityGroup = new ec2.SecurityGroup(this, 'ECSSecurityGroup', {
      vpc,
      description: 'Security group for the MemeVote ECS service',
      allowAllOutbound: true,
    });

    // Allow ECS to connect to the database
    dbSecurityGroup.addIngressRule(
      ecsSecurityGroup,
      ec2.Port.tcp(3306),
      'Allow ECS service to connect to MySQL'
    );

    // Also allow connections from the entire VPC CIDR for development
    dbSecurityGroup.addIngressRule(
      ec2.Peer.ipv4(vpc.vpcCidrBlock),
      ec2.Port.tcp(3306),
      'Allow VPC connections to MySQL for development'
    );

    // Also allow ECS security group to connect to itself (for load balancer health checks)
    ecsSecurityGroup.addIngressRule(
      ecsSecurityGroup,
      ec2.Port.tcp(8080),
      'Allow ALB to connect to ECS containers'
    );

    // Create Aurora Serverless v2 cluster for automatic scaling and cost optimization
    const dbCluster = new rds.DatabaseCluster(this, 'MemeVoteDatabase', {
      engine: rds.DatabaseClusterEngine.auroraMysql({
        version: rds.AuroraMysqlEngineVersion.VER_3_08_2,
      }),
      serverlessV2MinCapacity: 0.5, // Minimum capacity units (0.5 = 1GB RAM)
      serverlessV2MaxCapacity: isProd ? 4 : 1, // Maximum capacity units
      vpc,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC, // Use public subnets in default VPC
      },
      securityGroups: [dbSecurityGroup],
      defaultDatabaseName: config.dbName,
      credentials: rds.Credentials.fromGeneratedSecret(config.dbUsername, {
        secretName: `${config.projectName}-${config.environment}-db-credentials`,
      }),
      backup: {
        retention: cdk.Duration.days(isProd ? 30 : 7),
      },
      removalPolicy: isProd ? cdk.RemovalPolicy.SNAPSHOT : cdk.RemovalPolicy.DESTROY,
      deletionProtection: isProd,
      writer: rds.ClusterInstance.serverlessV2('writer', {
        publiclyAccessible: !isProd, // Make publicly accessible for development
      }),
      readers: isProd ? [rds.ClusterInstance.serverlessV2('reader')] : [],
    });

    // Create the ECS cluster
    const cluster = new ecs.Cluster(this, 'MemeVoteCluster', {
      vpc,
      containerInsights: true,
    });

    // Using Fargate for simplicity - no need for EC2 capacity providers

    // Create a task definition for the backend service
    const taskDefinition = new ecs.FargateTaskDefinition(this, 'MemeVoteTaskDef', {
      memoryLimitMiB: config.ecsTaskMemory,
      cpu: config.ecsTaskCpu,
    });

    // Add container to the task definition
    const container = taskDefinition.addContainer('MemeVoteContainer', {
      image: ecs.ContainerImage.fromAsset('../backend', {
        platform: ecrAssets.Platform.LINUX_AMD64, // Force x86_64 architecture
        buildArgs: {
          '--platform': 'linux/amd64',
        },
        extraHash: 'force-amd64-rebuild', // Force rebuild with new platform
      }),
      logging: ecs.LogDrivers.awsLogs({
        streamPrefix: 'memevote',
        logRetention: logs.RetentionDays.ONE_WEEK,
      }),
      environment: {
        SPRING_DATASOURCE_URL: `jdbc:mysql://${dbCluster.clusterEndpoint.hostname}:3306/${config.dbName}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`,
        SPRING_PROFILES_ACTIVE: config.environment === 'dev' ? 'staging' : 'prod',
        AWS_REGION: this.region,
        DB_SECRET_NAME: `${config.projectName}-${config.environment}-db-credentials`,
        CORS_ALLOWED_ORIGINS: `https://${config.frontendSubdomain}.${config.domainName}`,
      },
    });

    // Add port mapping
    container.addPortMappings({
      containerPort: 8080,
    });

    // Grant the task permission to read the database secret
    dbCluster.secret?.grantRead(taskDefinition.taskRole);

    // Grant additional permissions for AWS Secrets Manager service
    taskDefinition.taskRole.addManagedPolicy(
      iam.ManagedPolicy.fromAwsManagedPolicyName('SecretsManagerReadWrite')
    );

    // Look up the hosted zone
    const hostedZone = route53.HostedZone.fromLookup(this, 'HostedZone', {
      domainName: config.domainName,
    });

    // Create a certificate for the backend service
    const backendCertificate = new acm.Certificate(this, 'BackendCertificate', {
      domainName: `${config.backendSubdomain}.${config.domainName}`,
      validation: acm.CertificateValidation.fromDns(hostedZone),
    });

    // Create the Fargate service with an Application Load Balancer
    const backendService = new ecsp.ApplicationLoadBalancedFargateService(this, 'MemeVoteService', {
      cluster,
      taskDefinition,
      desiredCount: 1, // Start with 1 instance, will scale to 0 via auto-scaling
      publicLoadBalancer: true,
      assignPublicIp: true, // Required for public subnets in default VPC
      securityGroups: [ecsSecurityGroup],
      certificate: backendCertificate,
      domainName: `${config.backendSubdomain}.${config.domainName}`,
      domainZone: hostedZone,
      redirectHTTP: true,
      healthCheckGracePeriod: cdk.Duration.seconds(120), // Increased grace period for database startup
    });

    // Ensure ECS service waits for database to be available
    backendService.service.node.addDependency(dbCluster);

    // Configure health check to use Spring Boot Actuator health endpoint
    backendService.targetGroup.configureHealthCheck({
      path: '/actuator/health',
      healthyHttpCodes: '200',
      interval: cdk.Duration.seconds(30),
      timeout: cdk.Duration.seconds(5),
      healthyThresholdCount: 2,
      unhealthyThresholdCount: 3,
    });

    // Set up auto-scaling for the backend service (0 to 1 instances)
    const scaling = backendService.service.autoScaleTaskCount({
      minCapacity: 0, // Allow scaling to zero for cost optimization
      maxCapacity: 1, // Maximum 1 instance for low traffic
    });

    // Scale based on ALB request count instead of CPU for better responsiveness
    scaling.scaleOnRequestCount('RequestCountScaling', {
      requestsPerTarget: 10, // Scale up when more than 10 requests per target
      targetGroup: backendService.targetGroup,
      scaleInCooldown: cdk.Duration.minutes(5), // Wait 5 minutes before scaling down
      scaleOutCooldown: cdk.Duration.seconds(30), // Scale up quickly (30 seconds)
    });

    // Create an S3 bucket for the frontend
    const frontendBucket = new s3.Bucket(this, 'FrontendBucket', {
      bucketName: `${config.projectName}-${config.environment}-frontend`,
      websiteIndexDocument: 'index.html',
      websiteErrorDocument: 'index.html',
      publicReadAccess: false,
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      removalPolicy: isProd ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
      autoDeleteObjects: !isProd,
      cors: [
        {
          allowedMethods: [s3.HttpMethods.GET],
          allowedOrigins: [`https://${config.frontendSubdomain}.${config.domainName}`],
          allowedHeaders: ['*'],
        },
      ],
    });

    // Create a certificate for the frontend (must be in us-east-1 for CloudFront)
    const frontendCertificate = new acm.DnsValidatedCertificate(this, 'FrontendCertificate', {
      domainName: `${config.frontendSubdomain}.${config.domainName}`,
      hostedZone,
      region: 'us-east-1', // CloudFront requires certificates in us-east-1
    });

    // Create a CloudFront distribution for the frontend
    const frontendDistribution = new cloudfront.Distribution(this, 'FrontendDistribution', {
      defaultBehavior: {
        origin: origins.S3BucketOrigin.withOriginAccessControl(frontendBucket),
        viewerProtocolPolicy: cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
        allowedMethods: cloudfront.AllowedMethods.ALLOW_GET_HEAD,
        cachedMethods: cloudfront.CachedMethods.CACHE_GET_HEAD,
        cachePolicy: new cloudfront.CachePolicy(this, 'FrontendCachePolicy', {
          defaultTtl: config.frontendCachePolicy.defaultTtl,
          maxTtl: config.frontendCachePolicy.maxTtl,
          minTtl: config.frontendCachePolicy.minTtl,
          enableAcceptEncodingGzip: true,
          enableAcceptEncodingBrotli: true,
        }),
      },
      defaultRootObject: 'index.html', // Serve index.html for root requests
      errorResponses: [
        {
          httpStatus: 404,
          responseHttpStatus: 200,
          responsePagePath: '/index.html',
        },
        {
          httpStatus: 403,
          responseHttpStatus: 200,
          responsePagePath: '/index.html',
        },
      ],
      domainNames: [`${config.frontendSubdomain}.${config.domainName}`],
      certificate: frontendCertificate,
      priceClass: cloudfront.PriceClass.PRICE_CLASS_100, // Use only North America and Europe for cost savings
    });

    // Create a Route53 record for the frontend
    new route53.ARecord(this, 'FrontendARecord', {
      zone: hostedZone,
      recordName: config.frontendSubdomain,
      target: route53.RecordTarget.fromAlias(
        new targets.CloudFrontTarget(frontendDistribution)
      ),
    });

    // Deploy the frontend to S3
    new s3deploy.BucketDeployment(this, 'DeployFrontend', {
      sources: [s3deploy.Source.asset('../frontend/dist/frontend')],
      destinationBucket: frontendBucket,
      distribution: frontendDistribution,
      distributionPaths: ['/*'],
    });

    // Output the endpoints
    new cdk.CfnOutput(this, 'FrontendURL', {
      value: `https://${config.frontendSubdomain}.${config.domainName}`,
      description: 'Frontend URL',
    });

    new cdk.CfnOutput(this, 'BackendURL', {
      value: `https://${config.backendSubdomain}.${config.domainName}`,
      description: 'Backend URL',
    });

    new cdk.CfnOutput(this, 'DatabaseEndpoint', {
      value: dbCluster.clusterEndpoint.hostname,
      description: 'Aurora Serverless cluster endpoint',
    });

    new cdk.CfnOutput(this, 'DatabaseSecretArn', {
      value: dbCluster.secret?.secretArn || 'No secret created',
      description: 'Database credentials secret ARN',
    });
  }
}
