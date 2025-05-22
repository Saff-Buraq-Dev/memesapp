#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { MemesAppStack } from '../lib/memesapp-stack';

const app = new cdk.App();

const env = {
  account: '194722397154',
  region: 'ca-central-1',
};

new MemesAppStack(app, 'MemesAppStack', {
  env: env,
});