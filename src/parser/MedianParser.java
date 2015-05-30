package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class MedianParser {
	
	private static final String mTerm = "term";
	private static final String mCourse = "course";
	private static final String mSize = "size";
	private static final String mMedian = "median";
	private static final String mDept = "department";
	private static final String mCourseNumber = "course number";
	private static final String mSeason = "season";
	
	private static final String mIndex = "classes";
	private static final String mType = "classes";
	private static Node mNode;
	private static Client mClient;

	/**
	 * elasticsearch storage function
	 */
	public static void elasticSearchStorage(Map<String, Object> course){
		if (mNode != null && !mNode.isClosed()){
			String term, title, median;
			int size;
			
			term = (String) course.get(mTerm);
			title = (String) course.get(mCourse);
			size = (int) course.get(mSize);
			median = (String) course.get(mMedian);
			
			IndexRequest request = new IndexRequest(mIndex, mType)
					.source(course);
			mClient.index(request)
					.actionGet();
		}
	}
	
	/**
	 * class to parse text files that contain median grades
	 * files should be in the order of term \t course \t size \t median
	 * @param args - list of text files to parse
	 * @throws IOException 
	 */
	public static void main(String[] files) throws IOException {

		mNode = NodeBuilder.nodeBuilder()
				.client(true)
				.clusterName("ProjectAverage")
				.node();
		mClient = mNode.client();
		
		for (String path : files){
			System.out.println(path);
			File f = new File(path);
			if (f.exists()){
				InputStream in = new FileInputStream(f);
				int c, counter = 0;
				String word = "";
				HashMap<String, Object> course = new HashMap<String, Object>();
				
				// read the file character by character and get the words
				// words separated by a space, and classes separated by line
				while ((c = in.read()) != -1){
					char ch = (char) c;
					if (ch != '\n' && ch != '\t'){ // regular letter, add it to the word
						word += ch;
					} else if (ch == '\t'){ // new word, figure out which it is
						switch (counter){
							case (0):
								char season = word.charAt(2);
								course.put(mSeason, season);
								course.put(mTerm, word);
								counter++;
								word = "";
								continue;
							case (1):
								course.put(mCourse, word.substring(0, word.length() - 3));
								int startLoc = word.indexOf('-');
								String dept = word.substring(0, 4);
								String num = word.substring(5, 8);
								course.put(mDept, dept);
								course.put(mCourseNumber, num);
								counter++;
								word = "";
								continue;
							case (2):
								course.put(mSize, Integer.parseInt(word));
								counter++;
								word = "";
								continue;
						}
					} else {
						course.put(mMedian, word);	// get the last word
						elasticSearchStorage(course);
						word = "";
						counter = 0;
					}
				}
				in.close();
			}
		}
		mClient.close();
		mNode.close();
	}

}
