package bigdatacourse.hw2.studentcode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.datastax.oss.driver.api.core.CqlSession;

import bigdatacourse.hw2.HW2API;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import org.json.JSONArray;
import org.json.JSONObject;

public class HW2StudentAnswer implements HW2API{
	
	// general consts
	public static final String		NOT_AVAILABLE_VALUE 	=		"na";
	private static final String		TABLE_ITEMS				= 		"items";
	private static final String		TABLE_ITEM_REVIEWS 		= 		"item_reviews";
	private static final String		TABLE_USER_REVIEWS 		= 		"user_reviews";

	// CQL stuff
	private static final String		CQL_CREATE_ITEMS_TABLE =
			"CREATE TABLE " + TABLE_ITEMS 	+"(" 	+
					"asin			TEXT,"			+
					"title			TEXT,"			+
					"image			TEXT,"			+
					"categories		TEXT,"			+
					"description	TEXT,"			+
					"PRIMARY KEY ((asin))"			+
					") ";

	private static final String		CQL_CREATE_ITEM_REVIEWS_TABLE =
			"CREATE TABLE " + TABLE_ITEM_REVIEWS 	+"(" 					+
					"time				TIMESTAMP,"							+
					"asin				TEXT,"								+
					"reviewerID			TEXT,"								+
					"reviewerName		TEXT,"								+
					"rating				INT,"								+
					"summary			TEXT,"								+
					"reviewText			TEXT,"								+
					"PRIMARY KEY ((asin), time, reviewerID)"				+
					") "													+
					"WITH CLUSTERING ORDER BY (time DESC, reviewerID DESC)";

	private static final String		CQL_CREATE_USER_REVIEWS_TABLE =
			"CREATE TABLE " + TABLE_USER_REVIEWS 	+"(" 		            +
					"time				TIMESTAMP,"							+
					"asin				TEXT,"								+
					"reviewerID			TEXT,"								+
					"reviewerName		TEXT,"								+
					"rating				INT,"								+
					"summary			TEXT,"								+
					"reviewText			TEXT,"								+
					"PRIMARY KEY ((reviewerID),time, asin)"	                +
					") "						                            +
					"WITH CLUSTERING ORDER BY (time DESC, asin DESC)";
	
	// cassandra session
	private CqlSession session;

	// prepared statements
	private static final String		CQL_ITEM_INSERT =
			"INSERT INTO " + TABLE_ITEMS + "(asin, title, image, categories, description) VALUES(?, ?, ?, ?, ?)";

	private static final String		CQL_ITEM_REVIEW_INSERT =
			"INSERT INTO " + TABLE_ITEM_REVIEWS + "(time, asin, reviewerID, reviewerName, rating, summary, reviewText) VALUES(?, ?, ?, ?, ?, ?, ?)";

	private static final String		CQL_USER_REVIEW_INSERT =
			"INSERT INTO " + TABLE_USER_REVIEWS + "(time, asin, reviewerID, reviewerName, rating, summary, reviewText) VALUES(?, ?, ?, ?, ?, ?, ?)";

	private static final String		CQL_ITEM_SELECT =
			"SELECT * FROM " + TABLE_ITEMS + " WHERE asin = ?";

	private static final String		CQL_ITEM_REVIEW_SELECT =
			"SELECT * FROM " + TABLE_ITEM_REVIEWS + " WHERE asin = ?";

	private static final String		CQL_USER_REVIEW_SELECT =
			"SELECT * FROM " + TABLE_USER_REVIEWS + " WHERE reviewerID = ?";

	PreparedStatement pstmtAddItem;
	PreparedStatement pstmtAddItemReview;
	PreparedStatement pstmtAddUserReview;
	PreparedStatement pstmtSelectItem;
	PreparedStatement pstmtSelectItemReview;
	PreparedStatement pstmtSelectUserReview;

	@Override
	public void connect(String pathAstraDBBundleFile, String username, String password, String keyspace) {
		if (session != null) {
			System.out.println("ERROR - cassandra is already connected");
			return;
		}
		
		System.out.println("Initializing connection to Cassandra...");
		
		this.session = CqlSession.builder()
				.withCloudSecureConnectBundle(Paths.get(pathAstraDBBundleFile))
				.withAuthCredentials(username, password)
				.withKeyspace(keyspace)
				.build();
		
		System.out.println("Initializing connection to Cassandra... Done");
	}


	@Override
	public void close() {
		if (session == null) {
			System.out.println("Cassandra connection is already closed");
			return;
		}
		
		System.out.println("Closing Cassandra connection...");
		session.close();
		System.out.println("Closing Cassandra connection... Done");
	}

	
	
	@Override
	public void createTables() {
		session.execute(CQL_CREATE_ITEMS_TABLE);
		session.execute(CQL_CREATE_ITEM_REVIEWS_TABLE);
		session.execute(CQL_CREATE_USER_REVIEWS_TABLE);
		System.out.println("created tables: " + TABLE_ITEMS + ", "
											  + TABLE_ITEM_REVIEWS + ", "
											  + TABLE_USER_REVIEWS);
	}

	@Override
	public void initialize() {
		pstmtAddItem 			= 	session.prepare(CQL_ITEM_INSERT);
		pstmtAddItemReview 		= 	session.prepare(CQL_ITEM_REVIEW_INSERT);
		pstmtAddUserReview 		= 	session.prepare(CQL_USER_REVIEW_INSERT);
		pstmtSelectItem 		= 	session.prepare(CQL_ITEM_SELECT);
		pstmtSelectItemReview 	= 	session.prepare(CQL_ITEM_REVIEW_SELECT);
		pstmtSelectUserReview 	= 	session.prepare(CQL_USER_REVIEW_SELECT);
		System.out.println("Created prepare statements");
	}

	@Override
	public void loadItems(String pathItemsFile) throws Exception {
		int maxThreads	= 128;
		// creating the thread factors
		ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
		String line;
		BufferedReader br = new BufferedReader( new FileReader(pathItemsFile));
		List<String> string_debug = new ArrayList<String>();
		List<JSONObject> json_debug = new ArrayList<JSONObject>();
		while ((line = br.readLine()) != null) {
			TreeSet<String> categories_set = new TreeSet<String>();
			JSONObject item = new JSONObject(line);
			for (Object categories : item.getJSONArray("categories")) {
				JSONArray inner = (JSONArray) categories;
				for (Object cat : inner) {
					String s = (String) cat;
					categories_set.add(s);
				}
			}
			executor.execute(new Runnable() {
				@Override
				public void run() {
					BoundStatement bstmt = pstmtAddItem.bind()
							.setString("asin", item.getString("asin"))
							.setString("title", item.isNull("title")? NOT_AVAILABLE_VALUE: item.getString("title"))
							.setString("image", item.isNull("imUrl")? NOT_AVAILABLE_VALUE: item.getString("imUrl"))
							.setString("categories", categories_set.toString())
							.setString("description", item.isNull("description")? NOT_AVAILABLE_VALUE: item.getString("description"));
					session.execute(bstmt);
				}
			});
		}
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.HOURS);
	}

	@Override
	public void loadReviews(String pathReviewsFile) throws Exception {
		int maxThreads	= 128;
		// creating the thread factors
		ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
		String line;
		BufferedReader br = new BufferedReader( new FileReader(pathReviewsFile));
		List<String> string_debug = new ArrayList<String>();
		List<JSONObject> json_debug = new ArrayList<JSONObject>();
		Set <String> set = new HashSet<>();

		while ((line = br.readLine()) != null) {
			JSONObject item = new JSONObject(line);
			json_debug.add(item);
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						BoundStatement item_bstmt = pstmtAddItemReview.bind()
								.setString("asin", item.getString("asin"))
								.setInstant("time", Instant.ofEpochSecond(item.getInt("unixReviewTime")))
								.setString("reviewerID", item.getString("reviewerID"))
								.setString("reviewerName", item.isNull("reviewerName") ? NOT_AVAILABLE_VALUE : item.getString("reviewerName"))
								.setInt("rating", item.getInt("overall"))
								.setString("summary", item.isNull("summary") ? NOT_AVAILABLE_VALUE : item.getString("summary"))
								.setString("reviewText", item.isNull("reviewText") ? NOT_AVAILABLE_VALUE : item.getString("reviewText"));
						session.execute(item_bstmt);
						BoundStatement user_bstmt = pstmtAddUserReview.bind()
								.setInstant("time", Instant.ofEpochSecond(item.getInt("unixReviewTime")))
								.setString("asin", item.isNull("asin") ? NOT_AVAILABLE_VALUE : item.getString("asin"))
								.setString("reviewerID", item.isNull("reviewerID") ? NOT_AVAILABLE_VALUE : item.getString("reviewerID"))
								.setString("reviewerName", item.isNull("reviewerName") ? NOT_AVAILABLE_VALUE : item.getString("reviewerName"))
								.setInt("rating", item.getInt("overall"))
								.setString("summary", item.isNull("summary") ? NOT_AVAILABLE_VALUE : item.getString("summary"))
								.setString("reviewText", item.isNull("reviewText") ? NOT_AVAILABLE_VALUE : item.getString("reviewText"));
						session.executeAsync(user_bstmt);
					}catch (Exception e){
						System.out.println(e);
					}
				}
			});

		}
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.HOURS);
//		System.out.println("sent "+json_debug.size()+ " items");
//		System.out.println("set "+set.size()+ " items");
	}

	@Override
	public void item(String asin) {
		BoundStatement bstmt = pstmtSelectItem.bind().setString(0, asin);
		ResultSet rs = session.execute(bstmt);
		Row row = rs.one();
		if (row == null) {
			System.out.println("not exists");
			return;
		}
		System.out.println("asin: " 		+ row.getString("asin"));
		System.out.println("title: " 		+ row.getString("title"));
		System.out.println("image: " 		+ row.getString("image"));
		System.out.println("categories: "+    row.getString("categories"));
		System.out.println("description: " 	+ row.getString("description"));
		row = rs.one();
		assert row == null: "Item select returned more than one row";

//		// required format - example for asin B005QB09TU
//		System.out.println("asin: " 		+ "B005QB09TU");
//		System.out.println("title: " 		+ "Circa Action Method Notebook");
//		System.out.println("image: " 		+ "http://ecx.images-amazon.com/images/I/41ZxT4Opx3L._SY300_.jpg");
//		System.out.println("categories: " 	+ new TreeSet<String>(Arrays.asList("Notebooks & Writing Pads", "Office & School Supplies", "Office Products", "Paper")));
//		System.out.println("description: " 	+ "Circa + Behance = Productivity. The minute-to-minute flexibility of Circa note-taking meets the organizational power of the Action Method by Behance. The result is enhanced productivity, so you'll formulate strategies and achieve objectives even more efficiently with this Circa notebook and project planner. Read Steve's blog on the Behance/Levenger partnership Customize with your logo. Corporate pricing available. Please call 800-357-9991.");
//
//		// required format - if the asin does not exists return this value
//		System.out.println("not exists");
	}
	
	
	@Override
	public void userReviews(String reviewerID) {
		// the order of the reviews should be by the time (desc), then by the asin
		BoundStatement bstmt = pstmtSelectUserReview.bind().setString(0, reviewerID);
		ResultSet rs = session.execute(bstmt);
		Row row = rs.one();
		int count = 0;
		while (row != null) {
			System.out.println(
					"time: " 			+ row.getInstant("time")		 	+
					", asin: " 			+ row.getString("asin") 			+
					", reviewerID: " 	+ row.getString("reviewerID") 	+
					", reviewerName: " 	+ row.getString("reviewerName")	+
					", rating: " 		+ row.getInt("rating") 		+
					", summary: " 		+ row.getString("summary")		+
					", reviewText: " 	+ row.getString("reviewText")		);

			row = rs.one();
			count++;
		}
		System.out.println("total reviews: " + count);
		
//		// required format - example for reviewerID A17OJCRPMYWXWV
//		System.out.println(
//				"time: " 			+ Instant.ofEpochSecond(1362614400) +
//				", asin: " 			+ "B005QDG2AI" 	+
//				", reviewerID: " 	+ "A17OJCRPMYWXWV" 	+
//				", reviewerName: " 	+ "Old Flour Child"	+
//				", rating: " 		+ 5 	+
//				", summary: " 		+ "excellent quality"	+
//				", reviewText: " 	+ "These cartridges are excellent .  I purchased them for the office where I work and they perform  like a dream.  They are a fraction of the price of the brand name cartridges.  I will order them again!");
//
//		System.out.println(
//				"time: " 			+ Instant.ofEpochSecond(1360108800) +
//				", asin: " 			+ "B003I89O6W" 	+
//				", reviewerID: " 	+ "A17OJCRPMYWXWV" 	+
//				", reviewerName: " 	+ "Old Flour Child"	+
//				", rating: " 		+ 5 	+
//				", summary: " 		+ "Checkbook Cover"	+
//				", reviewText: " 	+ "Purchased this for the owner of a small automotive repair business I work for.  The old one was being held together with duct tape.  When I saw this one on Amazon (where I look for almost everything first) and looked at the price, I knew this was the one.  Really nice and very sturdy.");
//
//		System.out.println("total reviews: " + 2);
	}
	@Override
	public void itemReviews(String asin) {
		// the order of the reviews should be by the time (desc), then by the reviewerID
		BoundStatement bstmt = pstmtSelectItemReview.bind().setString(0, asin);
		ResultSet rs = session.execute(bstmt);
		Row row = rs.one();
		int count = 0;
		while (row != null) {
			System.out.println(
					"time: " 			+ row.getInstant("time")			+
					", asin: " 			+ row.getString("asin") 			+
					", reviewerID: " 	+ row.getString("reviewerID") 	+
					", reviewerName: " 	+ row.getString("reviewerName")	+
					", rating: " 		+ row.getInt("rating") 			+
					", summary: " 		+ row.getString("summary")		+
					", reviewText: " 	+ row.getString("reviewText")		);

			row = rs.one();
			count++;
		}
		System.out.println("total reviews: " + count);
		
		
//		// required format - example for asin B005QDQXGQ
//		System.out.println(
//				"time: " 			+ Instant.ofEpochSecond(1391299200) +
//				", asin: " 			+ "B005QDQXGQ" 	+
//				", reviewerID: " 	+ "A1I5J5RUJ5JB4B" 	+
//				", reviewerName: " 	+ "T. Taylor \"jediwife3\""	+
//				", rating: " 		+ 5 	+
//				", summary: " 		+ "Play and Learn"	+
//				", reviewText: " 	+ "The kids had a great time doing hot potato and then having to answer a question if they got stuck with the &#34;potato&#34;. The younger kids all just sat around turnin it to read it.");
//
//		System.out.println(
//				"time: " 			+ Instant.ofEpochSecond(1390694400) +
//				", asin: " 			+ "B005QDQXGQ" 	+
//				", reviewerID: " 	+ "AF2CSZ8IP8IPU" 	+
//				", reviewerName: " 	+ "Corey Valentine \"sue\""	+
//				", rating: " 		+ 1 	+
//				", summary: " 		+ "Not good"	+
//				", reviewText: " 	+ "This Was not worth 8 dollars would not recommend to others to buy for kids at that price do not buy");
//
//		System.out.println(
//				"time: "			+ Instant.ofEpochSecond(1388275200) +
//				", asin: " 			+ "B005QDQXGQ" 	+
//				", reviewerID: " 	+ "A27W10NHSXI625" 	+
//				", reviewerName: " 	+ "Beth"	+
//				", rating: " 		+ 2 	+
//				", summary: " 		+ "Way overpriced for a beach ball"	+
//				", reviewText: " 	+ "It was my own fault, I guess, for not thoroughly reading the description, but this is just a blow-up beach ball.  For that, I think it was very overpriced.  I thought at least I was getting one of those pre-inflated kickball-type balls that you find in the giant bins in the chain stores.  This did have a page of instructions for a few different games kids can play.  Still, I think kids know what to do when handed a ball, and there's a lot less you can do with a beach ball than a regular kickball, anyway.");
//
//		System.out.println("total reviews: " + 3);
	}


}
