-- Create the 'users' table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    salt TEXT NOT NULL,
    hashed_password TEXT NOT NULL,
    profile_picture_id INTEGER,
    FOREIGN KEY (profile_picture_id) REFERENCES picture(id)
);

-- Create the 'picture' table
CREATE TABLE IF NOT EXISTS picture (
    id INTEGER PRIMARY KEY,
    data BLOB NOT NULL,
    filetype TEXT NOT NULL
);

-- Create the 'file' table
CREATE TABLE IF NOT EXISTS file (
    id INTEGER PRIMARY KEY,
    filename TEXT NOT NULL,
    filetype TEXT NOT NULL,
    category TEXT,
    content BLOB NOT NULL,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create the 'sessions' table
CREATE TABLE IF NOT EXISTS sessions (
    id INTEGER PRIMARY KEY,
    id_session TEXT NOT NULL,
    username TEXT NOT NULL
);

-- Create the association table for the many-to-many relationship between files and users who voted
CREATE TABLE IF NOT EXISTS file_vote (
    file_id INTEGER,
    user_id INTEGER,
    FOREIGN KEY (file_id) REFERENCES file(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    PRIMARY KEY (file_id, user_id)
);
