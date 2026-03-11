## PostgreSQL setup on macOS with Homebrew

### 1. Install Homebrew

   Run:
   
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   
   Then add Homebrew to your shell:

         echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
         eval "$(/opt/homebrew/bin/brew shellenv)"
   

   Verify:
         
         brew --version


### 2. Install PostgreSQL 18

   Run:
   
         brew install postgresql@18


### 3. Add PostgreSQL binaries to PATH

   Because postgresql@18 is versioned, it is not added automatically.
   
   Run:
   
         echo 'export PATH="/opt/homebrew/opt/postgresql@18/bin:$PATH"' >> ~/.zshrc
         source ~/.zshrc
   
   Verify:

         psql --version
   
      Expected output: psql (PostgreSQL) 18.3 (Homebrew)


### 4. Fix locale settings

   Run:

         echo 'export LC_ALL=en_US.UTF-8' >> ~/.zshrc
         echo 'export LANG=en_US.UTF-8' >> ~/.zshrc
         source ~/.zshrc


### 5. Start PostgreSQL

   Try: 

         brew services start postgresql@18  (this might not work)
   
   If the command above doesn't work run the following: 

         pg_ctl -D /opt/homebrew/var/postgresql@18 start
   
  Expected output should end with: database system is ready to accept connections


### 6. Verify PostgreSQL is running

   Run: 

         pg_isready


### 7. Connect to PostgreSQL

   Run: 

         psql postgres

   Now you are inside PostgreSQL shell.


### 8. Create your application database

   Inside psql, run: 

         CREATE DATABASE shop_db;
   
   You can list databases with:
   
         \l
   
   Exit with:

         \q


### 9. If you want to stop the server run the following:

         brew services stop postgresql@18
   
   or 

         pg_ctl -D /opt/homebrew/var/postgresql@18 stop