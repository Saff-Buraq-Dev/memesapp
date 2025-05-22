from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import Column, Integer, String, ForeignKey, Table
from sqlalchemy.orm import relationship

db = SQLAlchemy()

file_vote_association = Table(
    'file_vote',
    db.Model.metadata,
    Column('file_id', Integer, ForeignKey('file.id'), primary_key=True),
    Column('user_id', Integer, ForeignKey('users.id'), primary_key=True)
)


class User(db.Model):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    username = Column(String(100), nullable=False, unique=True)
    email = Column(String(100), nullable=False, unique=True)
    salt = Column(String(32), nullable=False)
    hashed_password = Column(String(128), nullable=False)
    profile_picture_id = Column(String, ForeignKey('picture.id'))
    profile_picture = relationship(
        'Picture', foreign_keys=[profile_picture_id])
    files = relationship('File', back_populates='user')
    voted_files = relationship(
        'File',
        secondary=file_vote_association,
        back_populates='voters',
        lazy='dynamic'
    )

    def to_json(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
            'picture': self.profile_picture_id
        }

    def __repr__(self):
        return f"User(username={self.username}, email={self.email})"


class File(db.Model):
    id = Column(Integer, primary_key=True)
    filename = Column(String(120), nullable=False)
    filetype = Column(String(50), nullable=False)
    category = Column(String(50))
    content = Column(db.LargeBinary, nullable=False)
    user_id = Column(Integer, ForeignKey('users.id'), nullable=False)
    user = relationship('User', back_populates='files')
    voters = relationship(
        'User',
        secondary=file_vote_association,
        back_populates='voted_files',
        lazy='dynamic'
    )

    def to_json(self):
        return {
            'id': self.id,
            'filename': self.filename,
            'filetype': self.filetype,
            'category': self.category,
            'user_id': self.user_id,
            'voters': [voter.id for voter in self.voters],
            'voternames': [voter.username for voter in self.voters]
        }

    def __repr__(self):
        return f"File(filename={self.filename}, filetype={self.filetype}, category={self.category})"


class Picture(db.Model):
    id = db.Column(String, primary_key=True)
    data = db.Column(db.LargeBinary, nullable=False)
    filetype = db.Column(db.String(50), nullable=False)


class Session(db.Model):
    __tablename__ = 'sessions'
    id = Column(Integer, primary_key=True)
    id_session = Column(String(32))
    username = Column(String(100))

    def __init__(self, id, id_session, username):
        self.id = id
        self.id_session = id_session
        self.username = username

    def to_json(self):
        return {
            'id': self.id,
            'id_session': self.id_session,
            'username': self.username
        }

    @classmethod
    def find_by_id_session(cls, id_session):
        return cls.query.filter_by(id_session=id_session).first()
