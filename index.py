from flask import Flask, Response
from flask import render_template, redirect, jsonify, make_response, abort, send_file
from flask import request, session
from sqlalchemy import or_
from sqlalchemy.exc import IntegrityError
from messages import NOT_FOUND_MSG, INTERNAL_SERVER_ERROR_MSG, FORBIDDEN_MSG
from functools import wraps
from werkzeug.utils import secure_filename

import hashlib
import uuid
import json
import os
import io
from models.mymodels import User, Session, Picture, File, db

app = Flask(__name__, static_url_path='/', static_folder='static')

db_dir = "db/db.db"
SQLALCHEMY_DATABASE_URI = "sqlite:///" + os.path.abspath(db_dir)
app.config['SQLALCHEMY_DATABASE_URI'] = SQLALCHEMY_DATABASE_URI
app.secret_key = "(*&*&322387he738220)(*(*22347657"
db.init_app(app)


def authentication_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if not is_authenticated(session):
            return send_unauthorized()
        return f(*args, **kwargs)
    return decorated


def is_authenticated(session):
    # TODO Next-level : Vérifier la session dans la base de données
    return "id" in session


def send_unauthorized():
    return Response('Could not verify your access level for that URL.\n'
                    'You have to login with proper credentials.', 401,
                    {'WWW-Authenticate': 'Basic realm="Login Required"'})


@app.route('/')
def start_page():
    return render_template("accueil.html")


@app.route('/login', methods=["GET", "POST"])
def login():
    try:
        if request.method == "GET":
            return render_template('login.html', error=False)
        else:
            username_or_email = request.form.get("username")
            password = request.form.get("password")
            user = User.query.filter(or_(User.username == username_or_email,
                                         User.email == username_or_email)).first()
            if user is None:
                return render_template('login.html', error=True)
            salt = user.salt
            hashed_password = hashlib.sha512(str(password + salt)
                                             .encode("utf-8")).hexdigest()
            if hashed_password == user.hashed_password:
                id_session = uuid.uuid4().hex
                db.session.add(Session(None, id_session, user.username))
                db.session.commit()
                session["id"] = id_session
                session["user_id"] = user.id
            else:
                return render_template('login.html', error=True)
        return redirect(f"/users/{user.id}")
    except Exception as e:
        print(e)


@app.route('/users/<int:user_id>')
@authentication_required
def user_page(user_id):
    if "user_id" in session and session["user_id"] == user_id:
        user = User.query.filter_by(id=user_id).first()
        if user:
            return render_template('user.html', user=user.to_json())
        else:
            return render_template('custom-message.html', message='User not found'), 404
    else:
        return render_template('custom-message.html', message=FORBIDDEN_MSG), 403


@app.route('/myfiles')
@authentication_required
def my_files_page():
    return render_template('myfiles.html')


@app.route('/api/check_login')
def check_login():
    if is_authenticated(session):
        user = User.query.filter(User.id == session["user_id"]).first()
        if user:
            return jsonify({'logged_in': True, 'user': user.to_json()})

    return jsonify({'logged_in': False, 'user': ''})


@app.route('/api/myfiles/<int:user_id>', methods=['GET'])
def my_files(user_id):
    if is_authenticated(session):
        user = User.query.filter(User.id == user_id).first()
        if user:
            files = []
            for file in user.files:
                files.append(file.to_json())
            return jsonify({'files': files})


@app.route('/api/users/<int:user_id>/picture', methods=["PUT", "GET"])
@authentication_required
def upload_picture(user_id):
    try:
        # Rechercher l'utilisateur par nom d'utilisateur
        user = User.query.filter_by(id=user_id).first()
        if not user:
            return jsonify({'message': 'User not found'}), 404
        if request.method == "GET":
            binary_data = Picture.query.filter(
                Picture.id == user.profile_picture_id).first().data
            if binary_data is None:
                return Response(status=404)
            else:
                response = make_response(binary_data)
                response.headers.set('Content-Type', 'image/png')
            return response
        else:
            # Vérifier que le fichier est bien présent dans la requête
            if 'profile_pic' not in request.files:
                return jsonify({'message': 'No file part'}), 400

            # Récupérer le fichier de la requête
            file = request.files['profile_pic']

            # Vérifier que le fichier a bien été sélectionné
            if file.filename == '':
                return jsonify({'message': 'No selected file'}), 400

            # Lire les données du fichier
            picture_data = file.read()
            filetype = file.mimetype

            # Créer un nouvel enregistrement dans la table Picture
            new_picture = Picture(id=str(uuid.uuid4()),
                                  data=picture_data, filetype=filetype)
            db.session.add(new_picture)

            # Mettre à jour l'utilisateur pour faire référence à la nouvelle image
            user.profile_picture = new_picture

            # Enregistrer les modifications dans la base de données
            db.session.commit()

            return jsonify({'message': 'Profile picture updated successfully'}), 200
    except Exception as e:
        print(e)
        db.session.rollback()
        return jsonify({'message': str(e)}), 500


@app.route('/image/<pic_id>.png')
@authentication_required
def download_picture(pic_id):
    try:
        if "user_id" in session:
            user_id = session["user_id"]
            user = User.query.filter(User.id == user_id).first()
            if user.profile_picture_id != pic_id:
                return Response(status=403)
            binary_data = Picture.query.filter(
                Picture.id == pic_id).first().data
            if binary_data is None:
                return Response(status=404)
            else:
                response = make_response(binary_data)
                response.headers.set('Content-Type', 'image/png')
            return response
        else:
            return Response(status=403)
    except Exception as e:
        print(e)
        return Response(status=500)


@app.errorhandler(401)
def protected(e):
    print(e)
    return render_template('custom-message.html', message=FORBIDDEN_MSG), 401


@app.errorhandler(404)
def page_not_found(e):
    print(e)
    return render_template('custom-message.html', message=NOT_FOUND_MSG), 404


@app.errorhandler(Exception)
def internal_server_error(e):
    print(e)
    return render_template('custom-message.html',
                           message=INTERNAL_SERVER_ERROR_MSG), 500


@app.route('/signup', methods=["GET"])
def signup():
    return render_template('signup.html')


@app.route('/api/users', methods=["POST", "PUT"])
def add_user():
    if request.method == "POST":
        data = request.get_json()
        salt = uuid.uuid4().hex
        hashed_password = hashlib.sha512(str(data['password'] + salt)
                                         .encode("utf-8")).hexdigest()

        user = User(id=None, username=data['username'], email=data['email'],
                    salt=salt, hashed_password=hashed_password)
        try:
            db.session.add(user)
            db.session.commit()
            user = User.query.filter_by(username=data['username']).first()
            return jsonify(user.to_json()), 201
        except IntegrityError as e:
            db.session.rollback()
            error_info = e.orig.args[0]
            if "username" in error_info:
                return jsonify({'error': "Le nom d'utilisateur existe déjà."}), 409
            elif "email" in error_info:
                return jsonify({'error': "L'adresse email existe déjà"}), 409
    else:
        data = request.get_json()
        user = User.query.filter_by(id=data['id']).first()
        if user:
            user.email = data['email']
            user.username = data['username']
            session["user_id"] = user.id
            db.session.commit()
            return jsonify(user.to_json()), 200


@app.route('/logout')
@authentication_required
def logout():
    if 'id' in session:
        id_session = session['id']
        session_obj = Session.find_by_id_session(id_session)
        if session_obj:
            db.session.delete(session_obj)
            db.session.commit()
    session.clear()
    return redirect('/')


@app.route('/api/files', methods=['GET'])
def get_files():
    try:
        # Get query parameters
        filetype = request.args.get('filetype')
        user_id = request.args.get('user_id', type=int)
        page = request.args.get('page', default=1, type=int)
        per_page = request.args.get('per_page', default=10, type=int)

        query = File.query

        if filetype:
            query = query.filter(File.filetype == filetype)

        if user_id:
            query = query.filter(File.user_id == user_id)

        # Pagination
        paginated_files = query.paginate(
            page=page, per_page=per_page, error_out=False)

        files_with_votes = [
            {
                'id': file.id,
                'filename': file.filename,
                'filetype': file.filetype,
                'category': file.category,
                'user_id': file.user_id,
                'username': file.user.username,
                'votes_count': file.voters.count(),
                'voters': [voter.id for voter in file.voters],
                'voternames': [voter.username for voter in file.voters]
            }
            for file in paginated_files.items
        ]

        files_with_votes.sort(key=lambda x: x['votes_count'], reverse=True)

        result = {
            'page': paginated_files.page,
            'per_page': paginated_files.per_page,
            'total': paginated_files.total,
            'total_pages': paginated_files.pages,
            'files': files_with_votes
        }

        return jsonify(result)
    except Exception as e:
        print(e)
        return jsonify({'error': str(e)}), 500


@app.route('/api/files/<int:file_id>', methods=['GET'])
def get_file(file_id):
    file = File.query.get(file_id)

    if not file:
        abort(404, description="File not found")

    return send_file(
        io.BytesIO(file.content),
        mimetype=file.filetype,
        as_attachment=True,
        download_name=file.filename
    )


@app.route('/api/uploadfile', methods=['POST'])
@authentication_required
def upload_files():
    try:
        # Access all files and form data sent in the request
        files = request.files
        user_id = request.form.get('user_id')
        files_infos = request.form.get('files')
        if not user_id:
            print("No user ID provided")
            return jsonify({'error': 'No user ID provided'}), 400

        if not files:
            print("No files uploaded")
            return jsonify({'error': 'No files uploaded'}), 400

        files_infos = json.loads(files_infos)

        uploaded_files = []
        # Convert file storage to a list for indexing
        file_list = list(files.values())

        for i, file in enumerate(file_list):
            filename = file.filename
            if file and allowed_file(filename):
                # Get corresponding file_info
                if i < len(files_infos):
                    file_info = files_infos[i]
                else:
                    file_info = {}

                # Determine the filename and category
                actual_filename = file_info.get(
                    'filename', '').strip() or filename
                category = file_info.get('category', '').strip(
                ) or request.form.get('category', '')

                # Secure the filename
                secure_name = secure_filename(actual_filename)
                file_content = file.read()

                # Create a File object with the determined filename and category
                new_file = File(
                    filename=secure_name,
                    filetype=file.content_type,
                    category=category,
                    content=file_content,
                    user_id=user_id
                )

                db.session.add(new_file)
                db.session.flush()  # Ensures `new_file.id` is populated

                uploaded_files.append({
                    'filename': secure_name,
                    'file_id': new_file.id
                })

        db.session.commit()

        return jsonify({'message': 'Files uploaded successfully', 'files': uploaded_files}), 201

    except Exception as e:
        print(e)
        db.session.rollback()
        return jsonify({'error': str(e)}), 500


@app.route('/file/<int:file_id>', methods=['GET'])
def download_video(file_id):
    try:
        file = File.query.get(file_id)
        file = file.to_json()
        voters = file.get('voters', [])
        file['votes_count'] = len(voters)
        file['voters'] = voters
        file['username'] = User.query.filter_by(
            id=file['user_id']).first().username
        user_id = session.get('user_id')
        if user_id in file['voters']:
            file['voted'] = True
        else:
            file['voted'] = False
        if not file:
            print("File not found")
            abort(404, description="File not found")

        return render_template('file.html', item=file)
    except Exception as e:
        print(e)
        abort(500, description="Internal Server Error")


@app.route('/api/like/<user_id>/<int:file_id>', methods=['PUT'])
def vote(user_id, file_id):
    user = User.query.get(user_id)
    file = File.query.get(file_id)

    # Check if the user and file exist
    if not user or not file:
        return {'message': 'User or File not found'}, 404

    # Check if the user has already voted for the file
    if file in user.voted_files:
        return {'message': 'User has already voted for this file'}, 400

    # Add the file to the user's voted files
    user.voted_files.append(file)

    # Save the changes to the database
    try:
        db.session.commit()
        return {'message': 'Vote added successfully'}, 200
    except Exception as e:
        db.session.rollback()
        return {'message': 'An error occurred while adding the vote', 'error': str(e)}, 500


@app.route('/api/unlike/<user_id>/<int:file_id>', methods=['PUT'])
def unlike(user_id, file_id):
    # Retrieve the user and file from the database
    user = User.query.get(user_id)
    file = File.query.get(file_id)

    # Check if the user and file exist
    if not user or not file:
        return {'message': 'User or File not found'}, 404

    # Check if the user has voted for the file
    if file not in user.voted_files:
        return {'message': 'User has not voted for this file'}, 400

    # Remove the file from the user's voted files
    user.voted_files.remove(file)

    # Save the changes to the database
    try:
        db.session.commit()
        return {'message': 'Vote removed successfully'}, 200
    except Exception as e:
        db.session.rollback()
        return {'message': 'An error occurred while removing the vote', 'error': str(e)}, 500


def allowed_file(filename):
    allowed_extensions = {'jpg', 'jpeg', 'png', 'gif', 'mp4', 'avi', 'mov'}
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in allowed_extensions
