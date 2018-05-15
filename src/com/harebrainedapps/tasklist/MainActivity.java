package com.harebrainedapps.tasklist;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


/*
 * Steps to using the DB
 * 1. [DONE] Instantiate the DB Adapter by creating an instance of it
 * 2. [DONE] Open the DB
 * 3. [DONE] Use get, insert, delete, ...to change data
 * 4. [DONE] Close the DB
 * 
 * Steps to creating the task list
 * 5. [DONE] Make a Status field in the DB for task completion
 * 6. [DONE] Change ClearAll function to clear only items completed
 * 7. [DONE] Allow user to press Enter key to enter data into the DB
 * 8. [DONE] Create content to send via shareIt()
 * 9. [DONE] Get menu clear completed functional, then delete clear completed button
 * 10. Make a landscape layout
 * 11. Make a more sophisticated and unique UI
 * 12. Create a pop-up menu to delete or edit an individual task
 * 13. Create new fields in DB to store due date
 * 14. Create notifications to be sent to user if an item is due soon
 * 15. Activate the camera (sheerly for learning purposes) and hook up with Flickr
 *
 */
public class MainActivity extends ActionBarActivity  {
	
	DBAdapter myDb;
	
	Button mButton;
	EditText mEdit;
	TextView mText;
	String task;
	int status;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//new function to open DB, ctrl+1 creates method automatically after typing the declaration
		openDB();
		populateListViewFromDB();
		registerListClickCallBack();
		captureKeyListener();
		

		// addRecord button listener
		 mButton = (Button)findViewById(R.id.AddRecord);
	     mButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		 mEdit  = (EditText)findViewById(R.id.task_input);
         		task = mEdit.getText().toString();
         		if (task.length() != 0) {
	         		status = 0;
	         		onClick_AddRecord(view, task, status);
	         		mEdit.setText("");
         		}
        	}
        });	 
	}
	
	 @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
   
        return true;
    }
	 
	 @Override
     public boolean onOptionsItemSelected(MenuItem item) {
	 
        switch (item.getItemId()) {
        
        case R.id.menu_settings:
	   
	          return true;
	          
        case R.id.menu_clearCompleted:
        	
        	onClick_ClearCompleted();
        	
        	return true;
        	
        case R.id.menu_share:
        	
        	shareIt();
        	
        	return true;  
        }
        return false;
    } 
	 
	 // Sharing method
	 private void shareIt() {
		 
		 // Create a send Intent
		 Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

         // Get the sharing type
         shareIntent.setType("text/plain");
         
         // Create content to share
         String shareBody = populateShareBody();
         
         
         // Pass content to the intent
         shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Tasks");
         shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

         // Let the user choose their sharing medium
         startActivity(Intent.createChooser(shareIntent, "Share via"));

		}
	 

	// Allows user to press enter key on keyboard to save task to the DB
	public void captureKeyListener() {
        // retrive the edittext component
        mEdit = (EditText) findViewById(R.id.task_input);
        
        // add a keylistener to keep track user input
        mEdit.setOnKeyListener(new OnKeyListener(){
        public boolean onKey(View v, int keyCode, KeyEvent event) {     
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) 
                {
                   task = mEdit.getText().toString();
                   status = 0;
                   onClick_AddRecord(v, task, status);
                   mEdit.setText("");
                   return true;
                }
                return false;
        }
     });
   }
	
	// populate share body with existing task list from DB
	public String populateShareBody() {
		
		int numRows = myDb.getNumberOfRows();
		String[] shareElements = new String[numRows];
		String shareBody = "";
		
		Cursor cursor = myDb.getAllRows();
		cursor.moveToFirst();
		int i = 0;
		
		while(cursor.isAfterLast() == false) {
				
				String task = cursor.getString(DBAdapter.COL_TASK);
			
				shareElements[i] = task;
				i++;
				cursor.moveToNext();
		}
		cursor.close();
		
		for(int j = 0; j < shareElements.length; j++)
			shareBody =shareBody + (j + 1)+ ". "+ shareElements[j] + "\n";
		
		return shareBody;
	}

	// To close the database you can use a function onDestroy that will close the database when the application is terminated
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		
		closeDB();
	}
		
	private void closeDB() {
		myDb.close();
		
	}
	
	private void openDB() {
		
		//instance of myDB needs to take in a Context ctx of what to work with so we give it this
		myDb = new DBAdapter(this);
		// open the DB
		myDb.open();
		
		//populate the list from the database
		populateListViewFromDB();
	}


	// add a record to the database by using an instance of DBAdapter to call the method insertRow()
	public void onClick_AddRecord(View v, String task, int status) {
		
		// insertRow() returns a long which is the id number
		long newId = myDb.insertRow(task, status);
		populateListViewFromDB();
		
		
	}
	
	// Clear Completed Tasks
	public void onClick_ClearCompleted() {
		
		int numRows = myDb.getNumberOfRows();
		long[] idOfCompleted = new long[numRows];
		Cursor cursor = myDb.getAllRows();
		cursor.moveToFirst();
		int i = 0;
		
		while(cursor.isAfterLast() == false) {
				long idDb = cursor.getLong(DBAdapter.COL_ROWID);
				int status = cursor.getInt(DBAdapter.COL_STATUS);
				
				if(status == 1)
					idOfCompleted[i] = idDb;
				i++;
				cursor.moveToNext();
		}

		
		for(int j = 0; j < idOfCompleted.length; j++) {
			myDb.deleteRow(idOfCompleted[j]);
		}
		cursor.close();
		populateListViewFromDB();
	}
	
	
	private void populateListViewFromDB() {
		
		Cursor cursor = myDb.getAllRows();
		
		
		// Allow Activity to manage the lifetime of the cursor.
		// DEPRECATED! Runs on the UI thread.  OK for small/short queries.
		startManagingCursor(cursor);
		
		// Setup mapping of cursor to fields:
		String[] fromFieldName = new String[] 
									{DBAdapter.KEY_TASK};
		int[] toViewIDs = new int[] {R.id.item_task};
		
		// Create an adapter to map columns from the DB to elements in the UI
		SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(
												this,   // Context
												R.layout.item_layout,  // row layout template
												cursor,    				// cursor (set of DB records)
												fromFieldName,			// DB column names
												toViewIDs				// View IDs to put info in
												);
		myCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				int status = cursor.getInt(myDb.COL_STATUS);
				
				if (columnIndex == myDb.COL_TASK)
				{
					TextView v = (TextView)view.findViewById(R.id.item_task);
					
					v.setPaintFlags(status == 1 
							? (v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG)
							: (v.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG));
				}
				
				return false;
			}
		});

		// Set adapter for the ListView.
		ListView myList = (ListView)findViewById(R.id.listViewFromDB);
		myList.setAdapter(myCursorAdapter);
		
	}
	
	// Listen for user to click a task, mark the task as completed by calling
	// updateItemForID() and display at Toast signifying the completion
	private void registerListClickCallBack() {
		
		ListView myList = (ListView)findViewById(R.id.listViewFromDB);
		myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View viewClicked,
					int position, long idInDB) {
				
				updateItemForId(idInDB);
			
			}
		});
	}
	
	
	private void updateItemForId(long idInDB) {
		Cursor cursor = myDb.getRow(idInDB);
		
			if(cursor.moveToFirst()) {
				long idDb = cursor.getLong(DBAdapter.COL_ROWID);
				String task = cursor.getString(DBAdapter.COL_TASK);
				int status = cursor.getInt(DBAdapter.COL_STATUS);
				
				if(status == 0)
					status = 1;
				else
					status = 0;
				
				myDb.updateRow(idInDB, task, status);
			}
			
		cursor.close();
		
		// we are repopulating based on the database list.  If you want to maintain the ui list order you need more code.
		populateListViewFromDB();
		
	}
	
	private void displayToastForId(long idInDB) {
		Cursor cursor = myDb.getRow(idInDB);
		
		if(cursor.moveToFirst()) {
			String task = cursor.getString(DBAdapter.COL_TASK);
			
			String message = "You completed " + task + "!";
			
			Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
		}
		
		cursor.close();
	}
}
