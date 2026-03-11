-- Create files table for storing uploaded images and files
CREATE TABLE IF NOT EXISTS files (
    id SERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    data BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create index on created_at for performance
CREATE INDEX IF NOT EXISTS idx_files_created_at ON files(created_at);

-- Add comments to table and columns
COMMENT ON TABLE files IS 'Stores uploaded files and images with binary data';
COMMENT ON COLUMN files.id IS 'Primary key - auto-generated file ID';
COMMENT ON COLUMN files.file_name IS 'Original filename of the uploaded file';
COMMENT ON COLUMN files.content_type IS 'MIME type of the file (e.g., image/jpeg, image/png)';
COMMENT ON COLUMN files.size IS 'File size in bytes';
COMMENT ON COLUMN files.data IS 'Binary data of the file';
COMMENT ON COLUMN files.created_at IS 'Timestamp when the file was uploaded';
COMMENT ON COLUMN files.updated_at IS 'Timestamp when the file was last updated';

