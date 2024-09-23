import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AdminManageDriver extends AppCompatActivity {

    private static final int ADD_DRIVER_REQUEST = 1;
    private RecyclerView recyclerView;
    private DriverAdapter adapter;
    private List<DriverModel> driverList;
    private Button btnAddDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_list);

        recyclerView = findViewById(R.id.recyclerViewDrivers);
        btnAddDriver = findViewById(R.id.btnAddDriver); // Add this button in your layout

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        driverList = new ArrayList<>();
        adapter = new DriverAdapter(driverList);
        recyclerView.setAdapter(adapter);

        btnAddDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverListActivity.this, AddDriverActivity.class);
                startActivityForResult(intent, ADD_DRIVER_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_DRIVER_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                String name = data.getStringExtra("driver_name");
                String contact = data.getStringExtra("driver_contact");
                String dateAdded = data.getStringExtra("date_added");

                // Add the new driver to the list and notify the adapter
                driverList.add(new Driver(name, contact, dateAdded));
                adapter.notifyItemInserted(driverList.size() - 1);
                recyclerView.scrollToPosition(driverList.size() - 1); // Scroll to the new item
            }
        }
    }
}
