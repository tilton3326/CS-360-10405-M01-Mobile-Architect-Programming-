package com.example.inventoryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * inventoryActivity - Main screen for managing inventory items
 * <p>
 *   - Display list of inventory items from SQLite database
 *   - Add new items via addItem activity
 *   - Delete items from database
 *   - Update item quantities directly in the list
 *   - SMS notifications for low stock
 */
public class inventoryActivity extends AppCompatActivity {

    private InventoryAdapter adapter; // RecyclerView adapter
    private ArrayList<InventoryItem> inventoryList; // Holds the inventory items

    // --- Database helper ---
    private DatabaseHelper dbHelper; // Handles SQLite database operations

    // --- SMS Constants ---
    private static final int SMS_PERMISSION_CODE = 100; // Request code for runtime SMS permission
    private static final int LOW_STOCK_THRESHOLD = 3; // Minimum quantity to trigger low-stock alert
    private static final String DEFAULT_PHONE_NUMBER = "17864516721"; // Replace with target phone number

    // --- ActivityResultLauncher for addItem activity ---
    private final ActivityResultLauncher<Intent> addItemLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Called when AddItemActivity finishes
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    // Extract item details from returned Intent
                    String partNumber = data.getStringExtra("partNumber");
                    String name = data.getStringExtra("name");
                    String bin = data.getStringExtra("bin");
                    int quantity = data.getIntExtra("quantity", 0);

                    // Create InventoryItem object and save to database
                    InventoryItem newItem = new InventoryItem(partNumber, name, bin, quantity);
                    if (dbHelper.addInventoryItem(newItem)) {
                        // Add new item to local list and notify RecyclerView
                        inventoryList.add(newItem);
                        adapter.notifyItemInserted(inventoryList.size() - 1);

                        // Check if quantity is low and alert
                        checkAndAlertLowInventory(name, quantity);

                        Toast.makeText(this, "Added: " + name, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Apply edge-to-edge display
        setupWindowInsets();

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize RecyclerView, adapter, and buttons
        initializeInventory();
    }

    /**
     * Setup edge-to-edge display
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Initialize RecyclerView, adapter, load inventory, and set up buttons
     */
    private void initializeInventory() {
        // Load all inventory items from database
        inventoryList = dbHelper.getAllInventoryItems();

        // Initialize RecyclerView
        // --- UI Components ---
        // Displays inventory items in a scrollable list
        RecyclerView recyclerView = findViewById(R.id.inventoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with delete and quantity change listeners
        adapter = new InventoryAdapter(
                inventoryList,
                position -> { // --- Delete item listener ---
                    InventoryItem item = inventoryList.get(position);
                    if (dbHelper.deleteInventoryItem(item.getPartNumber())) {
                        // Remove item from list and update RecyclerView
                        inventoryList.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                },
                new InventoryAdapter.OnQuantityChangeListener() { // Quantity change listener
                    @Override
                    public void onQuantityIncrease(int position) {
                        InventoryItem item = inventoryList.get(position);
                        item.setQuantity(item.getQuantity() + 1); // Increment locally

                        // Update quantity in database
                        if (dbHelper.updateInventoryQuantity(item.getPartNumber(), item.getQuantity())) {
                            adapter.notifyItemChanged(position); // Refresh UI
                        } else {
                            Toast.makeText(inventoryActivity.this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onQuantityDecrease(int position) {
                        InventoryItem item = inventoryList.get(position);
                        if (item.getQuantity() > 0) {
                            item.setQuantity(item.getQuantity() - 1); // Decrement locally

                            // Update quantity in database
                            if (dbHelper.updateInventoryQuantity(item.getPartNumber(), item.getQuantity())) {
                                adapter.notifyItemChanged(position); // Refresh UI

                                // Check for low inventory and alert if needed
                                checkAndAlertLowInventory(item.getName(), item.getQuantity());
                            } else {
                                Toast.makeText(inventoryActivity.this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(inventoryActivity.this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        recyclerView.setAdapter(adapter);

        // Initialize buttons
        Button addItemButton = findViewById(R.id.addNewItemButton);
        // Buttons for adding items and sending SMS
        Button sendSmsButton = findViewById(R.id.sendSmsButton);

        // Add new item button
        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, addItem.class);
            addItemLauncher.launch(intent); // Launch addItem activity
        });

        // Send SMS button
        sendSmsButton.setOnClickListener(v -> {
            if (hasSmsPermission()) {
                sendSmsMessage("Inventory App SMS notifications are now enabled!");
            } else {
                requestSmsPermission();
            }
        });
    }

    /**
     * Checks if an item's quantity is below threshold and sends alert via SMS
     * @param itemName Name of the inventory item
     * @param quantity Current quantity
     */
    private void checkAndAlertLowInventory(String itemName, int quantity) {
        if (quantity < LOW_STOCK_THRESHOLD) {
            String alertMessage = "Low inventory alert for " + itemName + ": only " + quantity + " left!";
            if (hasSmsPermission()) {
                sendSmsMessage(alertMessage); // Send SMS if permission granted
            } else {
                Toast.makeText(this, "Low stock! Enable SMS for alerts.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // SMS Permission Handling
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSmsMessage("SMS notifications enabled for Inventory App!");
                Toast.makeText(this, "SMS permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied. Notifications disabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sends an SMS message to the default phone number
     * @param message Text message to send
     */
    private void sendSmsMessage(String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(DEFAULT_PHONE_NUMBER, null, message, null, null);
            Toast.makeText(this, "Alert sent via SMS!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
