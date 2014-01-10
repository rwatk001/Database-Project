/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;


/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class EmbeddedSQL {

    // reference to physical database connection.
    private Connection _connection = null;
    static String username = null;
    static String password = null;
    static EmbeddedSQL esql = null;
    static String[][] sqlResult = null;

    // handling the keyboard inputs through a BufferedReader
    // This variable can be global for convenience.
    static BufferedReader in = new BufferedReader(
                            new InputStreamReader(System.in));

    /**
    * Creates a new instance of EmbeddedSQL
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
    public EmbeddedSQL(String dbname, String dbport, String user, 
            String passwd) throws SQLException {

        System.out.print("Connecting to database...");
        try {
            // constructs the connection URL
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println ("Connection URL: " + url + "\n");

            // obtain a physical connection
            this._connection = DriverManager.getConnection(url, user, passwd);
            System.out.println("Done");
        } catch (Exception e) {
            System.err.println("Error - Unable to Connect to Database: " + 
                 e.getMessage());
            System.out.println("Make sure you start postgres on this machine");
            System.exit(-1);
        }
    }

    /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
    public void executeUpdate (String sql) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the update instruction
        stmt.executeUpdate (sql);

        // close the instruction
        stmt.close ();
    }

    /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
    public int executeQuery (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

        /*
        ** obtains the metadata object for the returned result set.  The metadata
        ** contains row and column info.
        */
        ResultSetMetaData rsmd = rs.getMetaData ();
        int numCol = rsmd.getColumnCount ();
        int rowCount = 0;
        sqlResult = new String[10][numCol];

        // iterates through the result set and output them to standard out.
//         boolean outputHeader = true;
        while (rs.next()) {
//             if(outputHeader) {
//                 for(int i = 1; i <= numCol; i++) {
//                 System.out.print(rsmd.getColumnName(i) + "\t");
//                 }
//                 System.out.println();
//                 outputHeader = false;
//             }
            for(int i = 1; i <= numCol; ++i) {
                sqlResult[rowCount][i - 1] = rs.getString(i);
//                  System.out.print(rs.getString (i) + "\t");
            }
//             System.out.println();
            ++rowCount;
        }//end while
        stmt.close ();
        return rowCount;
    }

    public ResultSet getResultSet (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);
        return rs;
    }


    /**
    * Method to close the physical connection if it is open.
    */
    public void cleanup(){
        try {
            if(this._connection != null) {
                this._connection.close ();
            }
        } catch (SQLException e) {
        }
    }

    public static int checkProgramInput (String[] args) {
        if(args.length != 4) {
            System.err.println (
                "Usage: " + "java [-classpath <classpath>] " +
                EmbeddedSQL.class.getName () + 
                " <dbname> <port> <user> <passwd>");
            return -1;
        }
        return 0;
    }

    /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
    public static void main (String[] args) {
        if(checkProgramInput(args) == -1) {
            return;
        }

        Greeting();
        try {
            // use postgres JDBC driver.
            Class.forName("org.postgresql.Driver").newInstance();
            // instantiate the EmbeddedSQL object and creates a physical
            // connection.
            String dbname = args[0];
            String dbport = args[1];
            String user = args[2];
            String passwd = args[3];
            esql = new EmbeddedSQL (dbname, dbport, user, passwd);

            int initialMenuChoice = initialMenu();
            if(initialMenuChoice == 1) {
                loginMenu();
            } else if(initialMenuChoice == 2) {
                registerMenu();
            }
            
            while(true) {
                int userMenuChoice = userMenu();
                if(userMenuChoice == 1) {
                    wallMenu();
                } else if(userMenuChoice == 2) {
                    watchOnlineMenu();
                } else if(userMenuChoice == 3) {
                    orderMenu();
                } else if(userMenuChoice == 4) {
                    break;
                }
            }

        } catch(Exception e) {
            System.err.println (e.getMessage ());
        } finally {
            try{
                if(esql != null) {
                    System.out.print("Disconnecting from database...");
                    esql.cleanup ();
                    System.out.println("Done\n\nBye !");
                }
            } catch (Exception e) {
            }
        }
    }
   
    /**
      * Function that holds Greeting.
      */
    public static void Greeting() {
        System.out.println(
            "\n\n*******************************************************\n" +
            "                          Welcome      	               \n" +
            "*******************************************************\n");
    }

    /**
      * Function for initial menu. (Login or Register)
      * @return int returns 1 for login, 2 for register
      */
    public static int initialMenu() {
        while(true) {
            System.out.print(
                "\n1) Login" +
                "\t2) Register: ");
            int choice = 0;
            try {
                choice = Integer.parseInt(in.readLine());
            } catch (Exception e) {
            }

            if((choice == 1) || (choice == 2)) {
                return choice;
            } else {
                System.out.println("Your input is invalid!");
            }
        }
    }

    /**
      * Function for login menu. Calls on loginQuery.
      */
    public static void loginMenu() {
        boolean keepon = true;
        while(keepon) {
            try {
                System.out.print("\nUsername: ");
                username = in.readLine(); 
                System.out.print("Password: ");
                password = in.readLine();
                SUCheck();
                if(loginQuery() > 0) {
                    keepon = false;
                } else {
                    System.out.println("Your input is invalid!");
                }
            } catch (Exception e) {
            }
        }
    }

    /**
      * Function that queries database with username and password.
      * @return int returns 1 for successful login, 0 for failure.
      */
    public static int loginQuery() {
        try {
            String query = "SELECT * FROM users WHERE user_id='";
                        query += username + "' AND ";
            query += "password='" + password + "';";
            int rowCount = esql.executeQuery(query);
            return rowCount;
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        return 0;
    }

    public static int SUCheck() {
        try {
            String query = "SELECT * FROM super_user WHERE super_user_id='"
                         + username + "';";
            int rowCount = esql.executeQuery(query);
            if (rowCount > 0) {
                SUMenu();
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        return 0;
    }

    public static void SUMenu() {
        try{
            boolean exit = false;
            while(exit == false) {
                System.out.print("\n--Super User Access--" +
                "\n1) Delete User" +
                "   2) Delete Movie" +
                "   3) Add Movie" +
                "   4) User Menu: ");
                int choice = 0;
                choice = Integer.parseInt(in.readLine());
                switch (choice) {
                case 1: deleteUser();
                    break;
                case 2: deleteMovie();
                    break;
                case 3: buildMovie();
                    break;
                case 4: exit = true;
                    break;
                default: System.out.println("Your input is invalid!");
                }
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

        public static void deleteUser() {
        try{
            boolean exit = false;
            while(exit == false) {
                System.out.print("\tEnter a User ID: ");
                String delete = in.readLine();
                if(checkUserQuery(delete) == 0) {
                    System.out.println("Your input is invalid!");
                } else {
                    clearUser(delete);
                    String query = "DELETE FROM users WHERE user_id='"
                            + delete + "';";
                    esql.executeQuery(query);
                }
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void clearUser(String delete) {
        try{
            String query = "DELETE FROM follow WHERE user_id_to='"
                    + delete + "' OR user_id_from='" + delete + "';";
            esql.executeQuery(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void deleteMovie() {
         try{
            boolean exit = false;
            while(exit == false) {
                System.out.print("\tEnter a Video ID: ");
                String delete = in.readLine();
                if(checkVideoQuery(delete) == 0) {
                    System.out.println("Your input is invalid!");
                } else {
                    String query = "DELETE FROM video WHERE video_id='"
                            + delete + "';";
                    esql.executeQuery(query);
                }
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void addMovie(String title, int year, int oprice, 
            int dprice, int votes, int rating, String episode, int season) {
        try {
            int movNum = 0;
            String query = "SELECT max(video_id) FROM video;";
            ResultSet rs = esql.getResultSet(query);
            while (rs.next()) {
                movNum = rs.getInt("max") + 1;
            }
            if (season > 0) {
                query = "INSERT INTO video VALUES(" 
                    + movNum + ", '" + title + "', " + year + ", " + oprice 
                    + ", " + dprice + ", " + votes + ", " + rating + ", '"
                    + episode + "', " + season + ");";
                esql.executeQuery(query);
            } else {
                query = "INSERT INTO video VALUES(" 
                    + movNum + ", '" + title + "', " + year + ", " + oprice 
                    + ", " + dprice + ", " + votes + ", " + rating + ");";
                esql.executeQuery(query);
            }

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    public static void buildMovie() {
        try {
            System.out.print("\tEnter Title: ");
            String title = in.readLine();

            System.out.print("\tEnter Year: ");
            int year = Integer.parseInt(in.readLine());

            System.out.print("\tEnter Online Price: ");
            int oPrice = Integer.parseInt(in.readLine());

            System.out.print("\tEnter DVD Price: ");
            int dPrice = Integer.parseInt(in.readLine());

            System.out.print("\tEnter Votes: ");
            int votes = Integer.parseInt(in.readLine());

            int rating = 0;
            while (rating < 1 || rating > 10) {
                System.out.print("\tEnter Rating [1-10]: ");
                rating = Integer.parseInt(in.readLine());
                if (rating < 1 || rating > 10) {
                    System.out.println("Invalid input!");
                }
            }

            System.out.print("\tEnter Episode [0 if none]: ");
            String episode = in.readLine();

            System.out.print("\tEnter Season ID [0 if none]: ");
            int season = Integer.parseInt(in.readLine());

            addMovie(title, year, oPrice, dPrice, votes, rating, episode,
                    season);

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
      * Function for register menu. Calls on registerQuery.
      */
    public static void registerMenu() {
        try {
            do {
                System.out.print("\tEnter a User ID: ");
                username = in.readLine();
                if(checkUserQuery(username) == 1) {
                    System.out.println("Your input is invalid!");
                } else {
                    break;
                }
            } while(true);

            System.out.print("\tEnter a Password: ");
            password = in.readLine();

            System.out.print("\tEnter an Email: ");
            String email = in.readLine();

            System.out.print("\tEnter your Firstname: ");
            String firstname = in.readLine();

            System.out.print("\tEnter your Middlename (optional): ");
            String middlename = in.readLine();

            System.out.print("\tEnter your Lastname: ");
            String lastname = in.readLine();

            System.out.print("\tDo you want to add an Address?\n\t" + 
                    "Enter 'y' for yes, 'n' for no: ");
            String inputaddress = in.readLine();

            while (!(inputaddress.equals("y")) && !(inputaddress.equals("n"))) {
                System.out.print("\nPlease enter 'y' or 'n': ");
                inputaddress = in.readLine();   
            }

            String street1 = "",
                   street2 = "",
                   state = "",
                   country = "",
                   zip = "";
            if(inputaddress.equals("y")) {
                System.out.print("\tEnter Street1: ");
                street1 = in.readLine();

                System.out.print("\tEnter Street2: ");
                street2 = in.readLine();

                System.out.print("\tEnter State: ");
                state = in.readLine();

                System.out.print("\tEnter Country: ");
                country = in.readLine();

                System.out.print("\tEnter Zip Code: ");
                zip = in.readLine();

            }
            System.out.println("\tStarting balance is $0\n");
            registerQuery(email, firstname, middlename, lastname, street1, 
                    street2, state, country, zip);
        } catch (Exception e) {
        }
    }

    /**
      * Function that queries database and registers a user.
      * @param  email       the email address of the user
      * @param  firstname   the firstname of user
      * @param  middlename  the middlename of user (optional)
      * @param  lastname    the lastname of user
      * @param  street1     the street of user (optional)
      * @param  street2     the street of user (optional)
      * @param  state       the state of user (optional)
      * @param  country     the country of user (optional)
      * @param  zip         the zip of user (optional)
      */
    public static void registerQuery(String email, String firstname, 
            String middlename, String lastname, String street1, String street2, 
            String state, String country, String zip) {

        try {
            String query = "INSERT INTO USERS VALUES(" +
                "'" + username + "','" + password  + "','" + firstname
                 + "','" + middlename + "','" + lastname + "','" +  email 
                 + "','" + street1 + "','" + street2 + "','" + state + "','" 
                 + country + "','" + zip + "'," + "0);";  
            esql.executeQuery(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
      * Function that queries database for user existence.
      * @param  userToCheck the user to check in database
      * @return int returns 1 for success, 0 for failure
      */
    public static int checkUserQuery(String userToCheck) {
        try {
             String query = "SELECT * FROM users WHERE user_id='" 
                 + userToCheck + "';";
            int rowCount = esql.executeQuery(query);
            return rowCount;
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        return 0;        
    }

    public static int checkVideoQuery(String vidID) {
        try {
             String query = "SELECT * FROM video WHERE video_id='" 
                 + vidID + "';";
            int rowCount = esql.executeQuery(query);
            return rowCount;
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        return 0;        
    }

    /**
      * Function for the user menu.
      * @return int returns 1 for wall, 2 for watch, 3 for order, 4 for exit
      */
    public static int userMenu() {
        while(true) {
            System.out.print(
                "\n1) Personal Wall" +
                "   2) Watch Online" +
                "   3) Order" +
                "   4) Exit: ");
            int choice = 0;
            try {
                choice = Integer.parseInt(in.readLine());
            } catch (Exception e) {
            }

            if((choice == 1) || (choice == 2) || (choice == 3) || choice == 4) {
                return choice;
            } else {
                System.out.println("Your input is invalid!");
            }
        }
    }

    public static int checkOrder(int vidID) {
        int count = 0;
        try {
            if (vidID == 0) {
                String query = "SELECT * FROM orders WHERE video_type=1"
                    + "AND user_id='" + username + "';";
                count = esql.executeQuery(query);
                //System.out.println(count);
            } else {
                String query = "SELECT video_id FROM orders WHERE video_id=" 
                    + vidID + " AND video_type=1 AND user_id='" 
                    + username + "';";
                count = esql.executeQuery(query);
            }

        } catch (Exception e) {
        }
        return count;
    }

    public static void deleteOrder(int vidID) {
        try {
            String query = "DELETE FROM orders WHERE video_id=" 
                 + vidID + " AND video_type=1 AND user_id='" 
                 + username + "';";
            esql.executeQuery(query);
        } catch (Exception e) {
        }
    }

    public static void addWatched(int vidID) {
        try {
            String query = "SELECT video_id, user_id FROM watch WHERE " 
                    + "video_id=" + vidID + " AND user_id='" 
                    + username + "';";
            int count = esql.executeQuery(query);
            if (count == 0) {
                Date date = new Date();
                Timestamp ts = new Timestamp((date.getTime() / 1000) * 1000);
                query = "INSERT INTO watch VALUES('" 
                    + username + "', " + vidID + ", '" + ts + "');";
                esql.executeQuery(query);
            } else {
                Date date = new Date();
                Timestamp ts = new Timestamp((date.getTime() / 1000) * 1000);
                query = "UPDATE watch SET watch_time='" + ts + "' WHERE"
                    + " user_id='" + username + "' AND video_id= " 
                    + vidID + ";";
                esql.executeQuery(query);

            }
        } catch (Exception e) {
        }
    }

    public static void addLike(int vidID) {
        try {
            String query = "SELECT user_id, video_id FROM likes WHERE " 
                    + "video_id=" + vidID + " AND user_id='" 
                    + username + "';";
            int count = esql.executeQuery(query);
            Date date = new Date();
            Timestamp ts = new Timestamp((date.getTime() / 1000) * 1000);
            if (count == 0) {
                query = "INSERT INTO likes VALUES('" 
                            + username + "' , " + vidID + "," + ts + ");";
                esql.executeQuery(query);
            } else {
                query = "UPDATE likes SET like_time='" + ts + "' WHERE"
                    + " user_id='" + username + "' AND video_id= " 
                    + vidID + ";";
                esql.executeQuery(query);
            }
        } catch (Exception e) {
        }
    }

    public static void addRate(int vidID) {
        try {
            System.out.println("Enter Rating [1-10]: ");
            int rating = Integer.parseInt(in.readLine());
            if (rating < 1 || rating >10) {
                System.out.println("Invalid input!");
                return;
            }
            String query = "SELECT user_id, video_id FROM rate WHERE " 
                    + "video_id=" + vidID + " AND user_id='" 
                    + username + "';";
            int count = esql.executeQuery(query);
            if (count == 0) {
                Date date = new Date();
                Timestamp ts = new Timestamp((date.getTime() / 1000) * 1000);
                query = "INSERT INTO rate VALUES('" 
                            + username + "' , " + vidID + ", '" 
                            + ts + "', " + rating + ");";
                esql.executeQuery(query);
            } else {
                Date date = new Date();
                Timestamp ts = new Timestamp((date.getTime() / 1000) * 1000);
                query = "UPDATE rate SET rate_time='" + ts + "', rating="
                    + rating + "WHERE"
                    + " user_id='" + username + "' AND video_id= " 
                    + vidID + ";";
                esql.executeQuery(query);
            }
        } catch (Exception e) {
        }
    }

    public static void addComment(int vidID) {
        try {
            System.out.println("Enter Comment: ");
            String comment = in.readLine();
            int comNum = 0;
            String query = "SELECT max(comment_id) FROM comment;";
            ResultSet rs = esql.getResultSet(query);
            while (rs.next()) {
                comNum = rs.getInt("max") + 1;
                System.out.println(comNum);
            }
            Date date = new Date();
            Timestamp ts = new Timestamp((date.getTime() / 1000) * 1000);
            System.out.println(comNum);
            query = "insert into comment values("
                    + comNum + ", '" + username + "', " + vidID
                    + ", '" + ts + "', '" + comment + "');";
            esql.executeQuery(query);
        } catch (Exception e) {
        }
    }

    public static void watchOnlineMenu() {
        try {
            while(true) {
                int checkOrder = checkOrder(0);
                //System.out.println(checkOrder);
                if (checkOrder == 0) {
                    System.out.println("\nYou have no online movies" 
                            + " in your cart");
                    break;
                }

                System.out.print("Enter a movie to watch: ");
                String title = in.readLine();
                int vidID = movieIDQuery(title);
                if (vidID == 0) {
                    System.out.println("\nMovie does not exist");
                    break;
                }
                checkOrder = checkOrder(vidID);
                if (checkOrder == 0) {
                    System.out.println("\nYou have not ordered that movie");
                    break;
                }
                while (true) {
                    System.out.print(
                        "\nYou are now watching " + title + " online!" +
                        "\n1) Like" +
                        "   2) Rate" +
                        "   3) Comment" +
                        "   4) Back: ");
                    deleteOrder(vidID);
                    addWatched(vidID);
                    int choice = Integer.parseInt(in.readLine());
                    switch (choice) {
                        case 1: addLike(vidID);
                            break;
                        case 2: addRate(vidID);
                            break;
                        case 3: addComment(vidID);
                            break;
                        case 4: return;
                        default: System.out.println("Your input is invalid!");
                            break;
                    }
                }   
            }
            } catch (Exception e) {
        }
    }

    /**
      * Function for the wall menu. Calls on different queries/menu.
      */
    public static void wallMenu() {
        while(true) {
            int choice = 0;
            while(true) {
                System.out.print(
                    "\n1) See Updates" +
                    "  2) Follow User " +
                    "  3) Favorites " +
                    "  4) Permissions " +
                    "  5) Balance " +
                    "  6) Back: ");
                try {
                    choice = Integer.parseInt(in.readLine());
                } catch (Exception e) {
                }

                if((choice == 1) || (choice == 2) || (choice == 3) || 
                        (choice == 4) || (choice == 5) || (choice == 6)){
                    break;
                } else {
                    System.out.println("Your input is invalid!");
                }
            }

            if(choice == 1) {
                seeUpdatesQuery();
            }

            if(choice == 2) {
                followUserMenu();
            }

            if(choice == 3) {
                favoritesMenu();
            }

            if(choice == 4) {
                permissionMenu();
            }

            if(choice == 5) {
                balanceMenu();
            }
            
            if(choice == 6) {
                return;
            }
        }
    }

    /**
      * Function that queries the database for updates for the user
      */
    public static void seeUpdatesQuery() {
        try {
            String query = null;
            query = "ALTER TABLE watch ADD temprating INT DEFAULT -1; "
                + "ALTER TABLE watch ADD type VARCHAR(9) DEFAULT 'watched'; " 
                + "ALTER TABLE rate ADD type VARCHAR(9) DEFAULT 'rated'; " 
                + "ALTER TABLE likes ADD temprating INT DEFAULT -1; "
                + "ALTER TABLE likes ADD type VARCHAR(9) DEFAULT 'liked'; "
                + "(SELECT A.user_id, B.title, A.rate_time, A.type, A.rating "
                + "FROM rate A, video B, permission C, follow D "
                + "WHERE A.video_id=B.video_id AND A.user_id=C.user_id AND "
                + "C.rate='public' AND D.user_id_to='" + username + "' AND "
                + "A.user_id=D.user_id_from UNION ALL "
                + "SELECT A.user_id,B.title,A.watch_time,A.type,A.temprating "
                + "FROM watch A, video B, permission C, follow D "
                + "WHERE A.video_id=B.video_id AND A.user_id=C.user_id AND "
                + "C.watch='public' AND D.user_id_to='" + username + "' AND "
                + "A.user_id=D.user_id_from) UNION ALL " 
                + "SELECT A.user_id,B.title,A.like_time,A.type,A.temprating "
                + "FROM likes A, video B, permission C, follow D "
                + "WHERE A.video_id=B.video_id AND A.user_id=C.user_id AND "
                + "C.likes='public' AND D.user_id_to='" + username + "' AND "
                + "A.user_id=D.user_id_from ORDER BY 3 DESC; "
                + "ALTER TABLE watch DROP temprating; "
                + "ALTER TABLE watch DROP type; "
                + "ALTER TABLE rate DROP type; "
                + "ALTER TABLE likes DROP temprating; "
                + "ALTER TABLE likes DROP type; ";

            esql.executeQuery(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }

        for(int i = 0; i < 10; ++i) {
            if(sqlResult[i][0] == null) {
                break;
            }
            System.out.print("At " + sqlResult[i][2] + " " + sqlResult[i][0] +
                " " + sqlResult[i][3] + " " + sqlResult[i][1]);
            if(Integer.parseInt(sqlResult[i][4]) != -1) {
                System.out.print(" with " + sqlResult[i][4] + " stars");
            }
            System.out.println();
        }
    }

    /**
      * Function for the follow user menu. Calls on followUserQuery. Only
      * allows valid users to be followed.
      */
    public static void followUserMenu() {
        while(true) {
            try {
                System.out.print("\nFollow User\n\tUsername: ");
                String followUser = in.readLine();
                if(username.equals(followUser)) {
                    System.out.println("You cannot follow yourself!");
                } else if(checkUserQuery(followUser) == 1) {
                    followUserQuery(followUser);
                    break;
                } else {
                    System.out.println("User does not exist!");
                }
            } catch (Exception e) {
            }
        }
    }

    /**
      * Function that queries the database to follow a user.
      * @param  followUser  the user to follow
      */
    public static void followUserQuery(String followUser) {
        try {
            Date date = new Date();
            Timestamp ts = new Timestamp((date.getTime() / 1000) * 1000);
            String query = "INSERT INTO follow VALUES('" +
                username + "','" + followUser + "','" + ts + "');";
            esql.executeQuery(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
      * Function for the favorites menu. Calls on favoritesQuery. Only allows
      * valid movies to be favorited.
      */
    public static void favoritesMenu() {
        while(true) {
            try {
                System.out.print("\nFavorites\n\tMovie Title: ");
                String movie = in.readLine();
                int checkMovie = checkMovieQuery(movie);
                if(checkMovie == 1) {
                    favoritesQuery(movie);
                    break;
                } else if(checkMovie == 2) {
                    System.out.println("Movie is already favorited!");
                } else {
                    System.out.println("Movie does not exist!");
                }
            } catch (Exception e) {
            }
        }
    }

    /**
      * Function that queries the database and inserts the user's favorite
      * movie choice.
      * @param  movie   the movie to be favorited
      */
    public static void favoritesQuery(String movie) {
        try {
            Date date = new Date();
            Timestamp ts = new Timestamp((date.getTime() / 1000) * 1000);
            String query = "INSERT INTO likes(user_id, video_id, like_time)"
                + " SELECT '" + username + "' ,video.video_id," + "'" + ts 
                + "'" + " FROM video WHERE title='" + movie + "';";
            esql.executeQuery(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
      * Function that queries the database to check if movie exists.
      * @param  movieToCheck    the movie to check in database
      * @return int             returns 1 for success, 2 if already favorited,
      *                         0 for failure
      */
    public static int checkMovieQuery(String movieToCheck) {
        try {
            String query = "SELECT * FROM video WHERE title='" 
                 + movieToCheck + "';";
            int rowCount = esql.executeQuery(query);

            query = "SELECT * FROM likes WHERE user_id='" + username + 
                "' AND video_id=(SELECT video_id FROM video WHERE title='" 
                + movieToCheck + "');";

            int rowCount2 = esql.executeQuery(query);

            query = "SELECT B.title FROM likes A, video B WHERE user_id='" + 
                username + "' AND A.video_id=B.video_id;";
            esql.executeQuery(query);
            if(sqlResult[0][0] != null) {
                System.out.println("\nRecent favorites:");
            }
            for(int i = 0;  i < 10; ++i) {
                if(sqlResult[i][0] == null) {
                    break;
                }
                System.out.println("\t" + sqlResult[i][0]);
            }
            System.out.println();
            if(rowCount2 != 0) {
                return 2;
            }
            return rowCount;
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        return 0;        
    }

    /**
      * Function for the permission menu. Calls on permissionQuery.
      */
    public static void permissionMenu() {
        int choice = 0;
        while(true) {
            System.out.print(
                "\n1) Favorites" +
                "\t2) Ranks " +
                "\t3) Watched " +
                "\t4) Playlist ");
            try {
                choice = Integer.parseInt(in.readLine());
            } catch (Exception e) {
            }

            if((choice == 1) || (choice == 2) || (choice == 3) || 
                    (choice == 4)){
                break;
            } else {
                System.out.println("Your input is invalid!");
            }
        }

        int choice2 = 0;
        while(true) {
            System.out.print(
                "\n1) Public" +
                "\t2) Private: ");
            try {
                choice2 = Integer.parseInt(in.readLine());
            } catch (Exception e) {
            }

            if((choice2 == 1) || (choice2 == 2)){
                break;
            } else {
                System.out.println("Your input is invalid!");
            }
        }

        permissionQuery(choice, choice2);
    }

    /**
      * Function that queries the database to change users permissions.
      * @param  choice  the input user provided (favorite, rank, watch,
      *                 playlist)
      * @param  choice2 public or private
      */
    public static void permissionQuery(int choice, int choice2) {
        try {
            String column = null;
            String value = null;
            if(choice == 1) {
                column = "likes";
            } else if(choice == 2) {
                column = "rate";
            } else if(choice == 3) {
                column = "watch";
            } else if(choice == 4) {
                column = "playlists";
            }

            if(choice2 == 1) {
                value = "public";
            } else if(choice2 == 2) {
                value = "private";
            }

            String query = "UPDATE permission SET " + column + "='" + value +
                "' WHERE user_id='" + username + "';";
            esql.executeQuery(query);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
      * Function for the balance menu. Calls on balanceQuery.
      */
    public static void balanceMenu() {
        int choice = 0;
        while(true) {
            System.out.print(
                "\n1) View Balance" +
                "\t2) Add Balance: ");
            try {
                choice = Integer.parseInt(in.readLine());
            } catch (Exception e) {
            }

            if((choice == 1) || (choice == 2)) { 
                break;
            } else {
                System.out.println("Your input is invalid!");
            }
        }
        
        int amount = 0;
        if(choice == 2) {
            while(true) {
                System.out.print(
                    "\n1) Add Amount: ");
                try {
                    amount = Integer.parseInt(in.readLine());
                } catch (Exception e) {
                }

                if(amount > 0) { 
                    break;
                } else {
                    System.out.println("Please enter atleast 1!");
                }
            }
        }
        balanceQuery(choice, amount);
    }

    /**
      * Function that queries the database for the user's balance.
      * @param  choice  view or add balance
      * @param  amount  if add balance, then the amount to be added
      */
    public static void balanceQuery(int choice, int amount) {
        try {
            String query = null;
            query = "SELECT balance FROM users WHERE user_id='" +
                username + "';";
            esql.executeQuery(query);
            Integer oldBalance = Integer.parseInt(sqlResult[0][0]);
            if(choice == 2) {
                amount += oldBalance;
                query = "UPDATE users SET balance=" + amount + 
                    " WHERE user_id='" + username + "';";
                esql.executeQuery(query);
            } else {
                System.out.println("Current Balance: " + oldBalance);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static int movieIDQuery(String movie) {
        try {
             String query = "SELECT video_id FROM video WHERE title='" 
                 + movie + "';";
            ResultSet rs = esql.getResultSet(query);

            while (rs.next()) {
                int vidID = rs.getInt("video_id");
                return vidID;
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        return 0;
    }

    public static String movieTitleQuery(int vidID) {
        try {
             String query = "SELECT title FROM video WHERE video_id='" 
                 + vidID + "';";

            ResultSet rs = esql.getResultSet(query);

            while (rs.next()) {
                String movie = rs.getString("title");
                return movie;
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        return "";
    }

    public static int moviePriceQuery(String movie, int type) {
        try {
            String price;
            if (type == 1) {
                price = "online_price";
            } else {
                price = "dvd_price";
            }
            String query = "SELECT " + price + " FROM video WHERE title='" 
                + movie + "';";
            ResultSet rs = esql.getResultSet(query);
            while (rs.next()) {
                int vidPrice = rs.getInt(price);
                return vidPrice;
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    public static int orderMenu() {
        try {
            int exit = -1;
            while(exit != 0) {
                System.out.print(
                "\n1) Purchase Movie" +
                "    2) View Cart " + 
                "    3) Back: ");

                int choice = Integer.parseInt(in.readLine());
                switch (choice) {
                case 1: purchaseMovie();
                    break;
                case 2: viewCart();
                    break;
                case 3: exit = 0;
                    break;
                default: System.out.println("Your input is invalid!");
                }
            }
        }catch (Exception e) {}
        return 0;
    }

    public static int purchaseMovie() {
        int exit = -1;
        while(exit != 0) {
            System.out.print(
                "\nSelect purchase type:" +
                "\n1) Online" +
                "\t2) DVD: ");
            int choice = 0;
            try {
                choice = Integer.parseInt(in.readLine());
            } catch (Exception e) {
            }

            if((choice == 1) || (choice == 2)) {
                exit = orderQuery(choice);
            } else {
                System.out.println("Your input is invalid!");
            }
        }
        return exit;
    }

    public static void addOrder(int vidID, int type) {
        try {
            int orderNum = 0;
            String query = "SELECT max(order_id) FROM orders;";
            ResultSet rs = esql.getResultSet(query);
            while (rs.next()) {
                orderNum = rs.getInt("max") + 1;
                //System.out.println(orderNum);
            }
            query = "INSERT INTO orders VALUES("
                    + orderNum + ", " + vidID + ", '" + username
                    + "', " + type + ");";
            esql.executeQuery(query);
        } catch (Exception e) {
        }
    }  

    public static int deductBalance(int vidPrice) {
         try {
            int balance = 0;
            String query = "SELECT balance FROM users WHERE user_id='" 
                + username + "';";
            ResultSet rs = esql.getResultSet(query);
            while (rs.next()) {
                balance = rs.getInt("balance");
            }
            balance -= vidPrice;
            if (balance < 0) {
                return -1;
            }

            query = "UPDATE users SET balance = " + balance 
                    + " WHERE user_id='" + username + "';";
            
            esql.executeQuery(query);
        } catch (Exception e) {
        }

        return 0;
    } 

    public static int orderQuery(int type) {
        try {
            if (type != 1 && type != 2) {
                return -1;
            }
            String input = "y";
            while(input == "y") {
                System.out.print("\nEnter the movie you wish to purchase: \n");
                String movie = in.readLine();
                int vidID = movieIDQuery(movie);
                int vidPrice = moviePriceQuery(movie, type);

                int balanceCheck = deductBalance(vidPrice);
                if (balanceCheck < 0) {
                    System.out.println("ERROR: Insufficient funds!");
                    return 0;
                }
                
                if (vidID == 0) {
                    System.out.println("Video does not exist!");
                    break;
                } else {
                    addOrder(vidID, type);
                    System.out.println("\nAdded to cart");
                    break;
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }
    
    public static void viewCart() {
        try{
            ArrayList<String> movieList = new ArrayList<String>();
            ArrayList<Integer> vidTypes = new ArrayList<Integer>();
            ArrayList<Integer> priceList = new ArrayList<Integer>();

            String query = "SELECT video_id FROM orders WHERE user_id='" 
                + username + "';";
            ResultSet rs = esql.getResultSet(query);
            while (rs.next()) {
                int vidID = rs.getInt("video_id");
                String movTitle = movieTitleQuery(vidID);
                movieList.add(movTitle);
            }

            query = "SELECT video_type FROM orders WHERE user_id='" 
                + username + "';";
            rs = esql.getResultSet(query);
            while (rs.next()) {
                int vidT = rs.getInt("video_type");
                vidTypes.add(vidT);
            }

            for (int i = 0; i < vidTypes.size(); i++){
                priceList.add(
                        moviePriceQuery(movieList.get(i), vidTypes.get(i))
                        );
            }

            for (int i = 0; i < movieList.size(); i++) {
                String type;
                if (vidTypes.get(i) == 1) {
                    type = "online";
                } else {
                    type = "dvd";
                }   

                System.out.println("\n" + (i+1) + ".\t" + movieList.get(i) 
                            + "\t" + type + "\t$" + priceList.get(i));   
            }

        } catch (Exception e) {
        }
        return;
    }
}
