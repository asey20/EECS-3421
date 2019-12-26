
import java.util.*;
import java.net.*;
import java.text.*;
import java.lang.*;
import java.io.*;
import java.sql.*;

/**
 * The SearchAndPurchase program implements an application that allows
 * the user to enter their custID and search find the best price for a
 * book in a category. If they choose to purchase the book at that price
 * they are asked for quantity of the purchased book and the purchase is
 * inserted into the database. The final price is displayed.
 * 
 * @author David Geller
 * @studID 214404255
 * @course EECS3421
 * @year F 2017
 */

public class SearchAndPurchase {
	private Connection conDB; // Connection to the database.
	private String url; // URL: Which database?

	private String checkID; // Check customer ID

	private Integer custID; // Customer ID
	private String custName; // Name of that customer.
	private String custCity; // Name of Customer's City

	private String title;
	private Integer year;
	private String language;
	private Integer weight;
	private String clubs;
	private Double bestPrice;
	private Integer quantity;

	private String inCat;
	private Integer count = 1; // counter for program step

	private ArrayList<String> catArr; // array for categories
	private ArrayList<String> titleArr; // array for titles

	private boolean bCat; // cat true or false?
	private boolean contains;

	private Integer option = 0;
	private String purchase = "";

	Map<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();

	/* *
	 * Method responsible for the command line interface of
	 * this program.
	 */
	public SearchAndPurchase() {
		// Set up the DB connection.
		try {
			// Register the driver with DriverManager.
			Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(0); // Who are we tallying?
		}

		// URL: Which database?
		url = "jdbc:db2:c3421a";

		// Initialize the connection.
		try {
			// Connect with a fall-thru id & password
			conDB = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.print("\nSQL: database connection error.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		// Let's have autocommit turned off. No particular reason here.
		try {
			conDB.setAutoCommit(false);
		} catch (SQLException e) {
			System.out.print("\nFailed trying to turn autocommit off.\n");
			e.printStackTrace();
			System.exit(0);
		}

		System.out.print("Enter Customer ID: ");
		Scanner scan = new Scanner(System.in); // Scans for input.
		Scanner scan2 = new Scanner(System.in); // Scans for input.

		checkID = scan.nextLine();

		while (!find_customer(checkID)) { // While id is not in db
			System.out.println("The ID cannot be found.");
			System.out.print("Enter Customer ID: ");
			checkID = scan.nextLine();
		}

		while (count == 1) {
			count = 0;
			catArr = new ArrayList<String>();
			titleArr = new ArrayList<String>();

			catArr = fetch_categories(); // cat to cat Array

			System.out.println();
			while (map.size() < 1) {
				System.out.println("Choose from the following Categories: \n");
				for (String list : catArr) {
					System.out.print(list + "\n");
				}
				System.out.println();
				System.out.print("Pick a Category: ");
				inCat = scan.nextLine().toLowerCase();
				System.out.println();

				while (!catArr.contains(inCat)) { // while category not in db
					System.out.println("Invalid Category!");
					System.out.println();
					System.out.println("Categories are:");

					for (String list : catArr) {
						System.out.print(list + "\n");
					}
					System.out.print("\n Pick a Category: ");
					inCat = scan.nextLine().toLowerCase();
				}

				System.out.println("Books in this category are:");
				System.out.println();
				titleArr = get_titles(inCat); // title to title Array

				for (String list : titleArr) {
					System.out.println(list);
				}

				System.out.println();
				System.out.print("Enter the title: ");
				title = scan.nextLine();
				bCat = find_book(title, inCat);

				if (map.size() == 0) {
					System.out.println();
					System.out.println("Book does not exist!");
					System.out.println();
				} else {
					while (option < 1 || option > map.size()) {
						System.out.println();
						System.out.print("Enter the book number: ");
						option = scan.nextInt();
						System.out.println();
					}

					// grabs book data for chosen book in map
					title = map.get(option).get(0);
					year = Integer.parseInt(map.get(option).get(1));
					language = map.get(option).get(2);
					weight = Integer.parseInt((map.get(option).get(3)));

					// Fetches minimum price
					contains = min_price(inCat, title, year, custID);
					fetch_club(bestPrice);
					System.out.printf("The minimum price is $%.2f\n", bestPrice);
					System.out.println();
				}
			}

			while (count < 2) {
				System.out.print("Do you want to purchase (y/n)? ");
				purchase = scan2.nextLine().toLowerCase();
				if (purchase.equals("y")) {
					while (count < 1) {
						System.out.print("Enter quantity: ");
						quantity = scan2.nextInt();

						while (quantity <= 0) {
							System.out.println("The number is invalid!");
							System.out.print("Enter quantity: ");
							quantity = scan2.nextInt();
						}
						
						insert_purchase(custID, clubs, title, year, quantity);
						System.out.println();
						System.out.print("You purchased " + title + " from " + clubs + " for ");
						System.out.printf("$%.2f\n", bestPrice * quantity);
						System.out.println();
						count = 1;
					}
					count = 2;
				} else if (purchase.equals("n")) {
					System.out.println("Goodbye!\n");
					System.out.println("   _   ");
					System.out.println(" _(\")_ ");
					System.out.println("(_ . _)");
				    System.out.println(" / : \\ ");
					System.out.println("(_/ \\_)\n");
					System.exit(0);
				} else {
					System.out.println("Invalid Option! \n");
				}
			}

		}

		scan.close(); // Close scanner 1.
		scan2.close(); // Close scanner 2.


		// Commit. Okay, here nothing to commit really, but why not...
		try {
			conDB.commit();
		} catch (SQLException e) {
			System.out.print("\nFailed trying to commit.\n");
			e.printStackTrace();
			System.exit(0);
		}

		// Close the connection.
		try {
			conDB.close();
		} catch (SQLException e) {
			System.out.print("\nFailed trying to close the connection.\n");
			e.printStackTrace();
			System.exit(0);
		}

	}
	
	/**
	 * This method is responsible for finding if the customer id is in the database.
	 * @param ID
	 * @return inDB
	 * @exception SQLException
	 */
	public boolean find_customer(String ID) {
		String queryText = ""; // The SQL text.
		PreparedStatement querySt = null; // The query handle.
		ResultSet answers = null; // A cursor.

		boolean inDB = false; // Return.

		queryText = "SELECT *" + "FROM yrb_customer " + "WHERE cid = ? ";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL#1 failed in prepare!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.setInt(1, Integer.parseInt(ID));
			answers = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL#1 failed in execute!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any answer?
		try {
			if (answers.next()) {
				inDB = true;
				custID = answers.getInt("cid");
				custName = answers.getString("name");
				custCity = answers.getString("city");
				System.out.println();
				System.out.println("Customer Name: " + custName + "\n" + "City: " + custCity);
			} else {
				inDB = false;
				custName = null;
			}
		} catch (SQLException e) {
			System.out.println("SQL failed in cursor!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing cursor!\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing the handle!\n");
			System.out.println(e.toString());

			System.exit(0);
		}

		return inDB;
	}

	/**
	 * This method is responsible for fetching the categories from the database.
	 * @param nothing
	 * @return cat
	 * @exception SQLException
	 */
	public ArrayList<String> fetch_categories() {
		String queryText = ""; // The SQL text.
		PreparedStatement querySt = null; // The query handle.
		ResultSet answers = null; // A cursor.
		ArrayList<String> cat = new ArrayList<String>();
		queryText = "SELECT *" + "FROM yrb_category ";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL failed in prepare!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			answers = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL failed in execute!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any answer?
		try {
			for (int i = 1; answers.next(); i++) {
				String category = answers.getString("cat");
				cat.add(category);
			}
		} catch (SQLException e) {
			System.out.println("SQL failed in cursor!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing cursor!\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing the handle!\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return cat;
	}

	/**
	 * This method is responsible for getting the titles fromt he chosen category
	 * @param categories
	 * @return title
	 * @exception SQLException
	 */
	public ArrayList<String> get_titles(String categories) {
		String queryText = ""; // The SQL text.
		PreparedStatement querySt = null; // The query handle.
		ResultSet answers = null; // A cursor.
		ArrayList<String> title = new ArrayList<String>();
		queryText = "SELECT distinct title " + " FROM yrb_book where cat = ? " + "AND title in (SELECT o.title FROM "
				+ "yrb_offer o " + "WHERE o.club in " + "(SELECT club " + "FROM yrb_member "
				+ "WHERE cid = ?)) AND year in " + "(SELECT o.year " + "FROM yrb_offer o "
				+ "WHERE o.club in (SELECT club " + "FROM yrb_member " + "WHERE cid = ?))";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL failed in prepare!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.setString(1, categories);
			querySt.setInt(2, custID);
			querySt.setInt(3, custID);
			answers = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL failed in execute!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any answer?
		try {
			for (int i = 1; answers.next(); i++) {
				String titles = answers.getString("title");
				title.add(titles);
			}
		} catch (SQLException e) {
			System.out.println("SQL failed in cursor!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing cursor!\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing the handle!\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return title;
	}

	/**
	 * This method is responsible for finding the book with the chosen title and category
	 * @param bookTitle, category
	 * @return inDB
	 * @exception SQLException
	 */
	public boolean find_book(String bookTitle, String category) {
		String queryText = ""; // The SQL text.
		PreparedStatement querySt = null; // The query handle.
		ResultSet answers = null; // A cursor.
		queryText = "SELECT * " + "FROM yrb_book " + "WHERE title = ?" + " AND cat = ?";
		boolean inDB = false;
		String titles;
		Integer weights;
		String languages;
		Integer years;
		int i;

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL failed in prepare!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.setString(1, bookTitle);
			querySt.setString(2, category);
			answers = querySt.executeQuery();
		} catch (SQLException e) {

			System.out.println("SQL failed in execute!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any answer?
		try {
			for (i = 0; answers.next(); i++) {
				titles = answers.getString("title");
				years = answers.getInt("year");
				languages = answers.getString("language");
				weights = answers.getInt("weight");
				map.put(i + 1, new ArrayList<String>(
						Arrays.asList(titles, Integer.toString(years), languages, Integer.toString(weights))));

			}
			if (i > 0) {
				inDB = true;
				int j = 1;
				while (j <= i) {
					titles = map.get(j).get(0);
					years = Integer.parseInt(map.get(j).get(1));
					languages = map.get(j).get(2);
					weights = Integer.parseInt((map.get(j).get(3)));
					System.out.println();
					System.out.println("Book " + j + " - | Title:  " + titles + " | Year: " + years + " | Language: "
							+ languages + " | Weight: " + weights + " |");
					j++;
				}
			} else {
				inDB = false;

			}

		} catch (SQLException e) {
			System.out.println("SQL failed in cursor!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing cursor!\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing the handle!\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		return inDB;
	}

	/**
	 * This method is responsible for finding the (club with the) best price of the book.
	 * @param category, titles, years, customerID
	 * @return inDB
	 * @exception SQLException
	 */
	public boolean min_price(String category, String titles, int years, int customerID) {
		String queryText = ""; // The SQL text.
		PreparedStatement querySt = null; // The query handle.
		ResultSet answers = null; // A cursor.
		// ArrayList<String> books=new ArrayList<String>();
		Double price = 0.0;
		boolean inDB = false;
		queryText = " SELECT min(price)" + "FROM yrb_offer WHERE title = ? AND year = ? "
				+ "AND club in (SELECT club FROM yrb_member WHERE cid = ?)";
		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL failed in prepare!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.setString(1, titles);
			querySt.setInt(2, years);
			querySt.setInt(3, customerID);
			answers = querySt.executeQuery();
		} catch (SQLException e) {

			System.out.println("SQL failed in execute!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any answer?
		try {
			if (answers.next()) {
				inDB = true;
				bestPrice = answers.getDouble(1);
			} else {
				inDB = false;
			}

		} catch (SQLException e) {
			System.out.println("SQL failed in cursor!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing cursor!\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing the handle!\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		return inDB;
	}

	/**
	 * This method helps find the club with the best price.
	 * @param price
	 * @return unused
	 * @exception SQLException
	 */
	private void fetch_club(double price) {
		String queryText = ""; // The SQL text.
		PreparedStatement querySt = null; // The query handle.
		ResultSet answers = null; // A cursor.

		queryText = "SELECT o.club FROM yrb_member m, yrb_offer o" + " WHERE o.club = m.club AND o.year = ? "
				+ "AND m.cid = ? AND o.title = ? AND o.price = ? ";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL failed in prepare!");
			System.out.println(e.toString());
			System.exit(0);
		}
		// Execute the query.
		try {
			querySt.setInt(1, year);
			querySt.setInt(2, custID);
			querySt.setString(3, title);
			querySt.setDouble(4, bestPrice);
			answers = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL failed in execute!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// see if it works
		try {
			if (answers.next()) {
				clubs = answers.getString(1);
			}
		} catch (SQLException e) {
			System.out.println("SQL failed in cursor!");
			System.out.println(e.toString());
			System.exit(0);
		}
		// Close the cursor.
		try {
			answers.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing cursor!\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing the handle!\n");
			System.out.println(e.toString());
			System.exit(0);
		}
	}
	
	/**
	 * This method inserts the finalized purchase into the database.
	 * @param cid, club, title, years, qnty
	 * @return unused
	 * @exception SQLException
	 */
	public void insert_purchase(int cid, String club, String titles, int years, int qnty) {
		String queryText = ""; // The SQL text.
		PreparedStatement querySt = null; // The query handle.
		ResultSet answers = null; // A cursor.

		Timestamp ts = new Timestamp(System.currentTimeMillis());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
		String time = df.format(ts);

		queryText = "Insert into yrb_purchase values (?,?,?,?,?,?) ";
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch (SQLException e) {
			System.out.println("SQL failed in prepare!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {

			querySt.setInt(1, cid);
			querySt.setString(2, club);
			querySt.setString(3, titles);
			querySt.setInt(4, years);
			querySt.setString(5, time);
			querySt.setInt(6, qnty);
			querySt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQL failed in update!");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL failed closing the handle!\n");
			System.out.println(e.toString());
			System.exit(0);
		}

	}

	public static void main(String[] args) {
		SearchAndPurchase SandP = new SearchAndPurchase(); // gets it going!
	}
}
