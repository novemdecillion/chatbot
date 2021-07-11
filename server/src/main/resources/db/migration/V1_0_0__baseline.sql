CREATE TABLE IF NOT EXISTS logs (
   id serial PRIMARY KEY,
   session_id VARCHAR (256) NOT NULL,
   kind VARCHAR (32) NOT NULL,
   ip_address VARCHAR (256),
   input TEXT,
   output TEXT,
   error TEXT,
   created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
