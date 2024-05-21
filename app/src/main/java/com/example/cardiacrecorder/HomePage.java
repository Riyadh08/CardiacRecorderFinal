package com.example.cardiacrecorder;

import static com.example.cardiacrecorder.viewmodel.FilterViewModel.DATE;
import static com.example.cardiacrecorder.viewmodel.FilterViewModel.DYS;
import static com.example.cardiacrecorder.viewmodel.FilterViewModel.HEART;
import static com.example.cardiacrecorder.viewmodel.FilterViewModel.SYS;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.cardiacrecorder.adapter.OptionAdapter;
import com.example.cardiacrecorder.adapter.RvAdapter;
import com.example.cardiacrecorder.classes.DataModel;
import com.example.cardiacrecorder.classes.EachData;
import com.example.cardiacrecorder.classes.MyDatePicker;
import com.example.cardiacrecorder.databinding.ActivityHomepageBinding;
import com.example.cardiacrecorder.others.AdapterListener;
import com.example.cardiacrecorder.roomDb.BoardViewModel;
import com.example.cardiacrecorder.viewmodel.FilterViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomePage extends AppCompatActivity {

    private ActivityHomepageBinding binding = null;
    private RvAdapter adapter = null;

    private BoardViewModel viewModel = null;
    private FilterViewModel filterViewModel = null;
    private BottomSheetBehavior<RelativeLayout> bottomSheetBehavior;

    private Toast mToast = null;

    private boolean isSheetShowing = false, isDoubleClickDone = false, isFromDate = false;
    private List<EachData> allData = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        initializeRoomRv();
        setUpFilter();
        setClickListener();
        startFilterModel();
    }

    /**
     * initializes bottomSheet
     */
    private void initialize(){
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
    }


    /**
     * sets filter
     */
    private void setUpFilter(){
        if(binding == null) return;


        MyDatePicker datePicker = new MyDatePicker(value ->{

            if(isFromDate) {
                binding.viewBottomSheet.tvFromDate.setText(value);
            }
            else{
                binding.viewBottomSheet.tvToDate.setText(value);
            }
        });

        binding.viewBottomSheet.clFromDate.setOnClickListener(view -> {
            isFromDate = true;
            datePicker.show(getSupportFragmentManager(),"Date picker");
        });

        binding.viewBottomSheet.clToDate.setOnClickListener(view -> {
            isFromDate = false;
            datePicker.show(getSupportFragmentManager(),"Date picker");
        });

        OptionAdapter sortByAdapter = new OptionAdapter(this,"Date","Sys","Dys","Heart");
        OptionAdapter sysAdapter = new OptionAdapter(this,"Low","Normal","High");
        OptionAdapter dysAdapter = new OptionAdapter(this,"Low","Normal","High");
        OptionAdapter hrAdapter = new OptionAdapter(this,"Low","Normal","High");


        binding.viewBottomSheet.rvSortBy.setAdapter(sortByAdapter);
        binding.viewBottomSheet.rvSys.setAdapter(sysAdapter);
        binding.viewBottomSheet.rvDys.setAdapter(dysAdapter);
        binding.viewBottomSheet.rvHeartRate.setAdapter(hrAdapter);

        binding.viewBottomSheet.buttonSaveFilter.setOnClickListener(view -> {

            String fromDate = String.valueOf(binding.viewBottomSheet.tvFromDate.getText());
            String toDate = String.valueOf(binding.viewBottomSheet.tvToDate.getText());

            int sortBy = sortByAdapter.getSelectedPosition();
            int sysBy = sysAdapter.getSelectedPosition();
            int dysBy = dysAdapter.getSelectedPosition();
            int heartBy = hrAdapter.getSelectedPosition();

            filterViewModel.setAll(fromDate,toDate,sortBy,sysBy,dysBy,heartBy);
            filterViewModel.setShowOrHide(false);
        });

        binding.viewBottomSheet.buttonClearFilter.setOnClickListener(view ->{
            if(binding == null) return;
            binding.viewBottomSheet.tvFromDate.setText(getString(R.string.dashed_date));
            binding.viewBottomSheet.tvToDate.setText(getString(R.string.dashed_date));
            sortByAdapter.resetFilter();
            sysAdapter.resetFilter();
            dysAdapter.resetFilter();
            hrAdapter.resetFilter();
        });

    }

    /**
     * starts filter model
     */
    private void startFilterModel(){
        filterViewModel = new ViewModelProvider(this).get(FilterViewModel.class);
        filterViewModel.getShowOrHide().observe(this, aBoolean -> {
            isSheetShowing = aBoolean;
            if(aBoolean){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            else{
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        filterViewModel.getTrigger().observe(this, ignore -> submitList());
    }

    /**
     * creates the list
     */
    private void submitList(){
        if(filterViewModel == null) {
            adapter.submitList(allData);
            return;
        }

        if(allData == null) return;

        if(binding == null) return;
        if(allData.isEmpty()){
            binding.tvNoData.setVisibility(View.VISIBLE);
        }
        else{
            binding.tvNoData.setVisibility(View.GONE);
        }

        if(filterViewModel == null) return;

        int sysBy = filterViewModel.getSysBy();
        int dysBy = filterViewModel.getDysBy();
        int heartBy = filterViewModel.getHeartBy();

        long startDate = EachData.getEpochDate(filterViewModel.getFromDate());
        long endDate = EachData.getEpochDate(filterViewModel.getToDate());

        if(endDate == Long.MIN_VALUE) endDate = Long.MAX_VALUE;

        List<EachData> matchedList = new ArrayList<>();
        for(EachData data : allData){
            if(data.isThisOK(sysBy,dysBy,heartBy) && data.getEpochDate() >= startDate && data.getEpochDate() <= endDate){
                matchedList.add(data);
            }
        }

        if(matchedList.isEmpty()) binding.tvNoData.setVisibility(View.VISIBLE);
        else binding.tvNoData.setVisibility(View.GONE);

        final int[] sortBy = {filterViewModel.getSortBy()};

        matchedList.sort((o1, o2) ->
                (sortBy[0] == DATE) ? (int)(o1.getEpochDate() - o2.getEpochDate()) :
                        (sortBy[0] == SYS) ? (o1.getSysPressure() - o2.getSysPressure()) :
                                (sortBy[0] == DYS) ? (o1.getDysPressure() - o2.getDysPressure()) :
                                        (sortBy[0] == HEART) ? (o1.getHeartRate() - o2.getHeartRate()) : (o1.getId() - o2.getId()));

        adapter.submitList(matchedList);
    }


    /**
     * toast shower
     * @param message to be shown
     */
    private void showSafeToast(String message){
        try {
            mToast.cancel();
        }catch (Exception ignored){}
        mToast = Toast.makeText(this,message,Toast.LENGTH_SHORT);
        mToast.show();
    }


    /**
     * set click listener to buttons
     */
    private void setClickListener(){
        binding.fabAdd.setOnClickListener(view -> {
            startActivity(new Intent(this,AdderActivity.class));
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        });

        binding.clFilterOption.setOnClickListener(view-> filterViewModel.setShowOrHide(true));

    }


    /**
     * initializes adapter, view-model and observe all data from room
     */
    private void initializeRoomRv(){
        adapter = new RvAdapter(this);
        binding.rvList.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(BoardViewModel.class);

        viewModel.getAllData().observe(this, allData -> {
            if(allData == null || allData.isEmpty()){
                allData = new ArrayList<>();
            }

            this.allData = allData;

            if(binding == null) return;
            if(allData.isEmpty()){
                binding.tvNoData.setVisibility(View.VISIBLE);
            }
            else{
                binding.tvNoData.setVisibility(View.GONE);
            }

            submitList();

        });

        adapter.setAdapterListener(new AdapterListener() {
            @Override
            public void onDeleteRequest(EachData data) {
                if(viewModel != null){
                    viewModel.delete(data);
                }
            }

            @Override
            public void onEditRequest(EachData data) {
                Intent intent = new Intent(HomePage.this,AdderActivity.class);
                intent.putExtra("data",data);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onShowRequest(EachData data) {
                Intent intent = new Intent(HomePage.this,DetailsPage.class);
                intent.putExtra("data",data);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void logOutUser(){
        showSafeToast(getString(R.string.please_wait));
        backupData(error -> {
            if(error != null){
                showSafeToast(error);
                return;
            }
            viewModel.deleteAll();
            FirebaseAuth.getInstance().signOut();
            SharedPreferences sp = getSharedPreferences("sp",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("amILoggedIn",false);
            editor.apply();

            Intent intent = new Intent(HomePage.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private interface BackupListener{
        void onBackupProcessed(String error);
    }
    private void backupData(BackupListener listener){
        if( allData == null || allData.isEmpty()) {
            listener.onBackupProcessed(null);
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getUid();
        if(userId == null){
            listener.onBackupProcessed(getString(R.string.failed_to_authenticate));
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("data").child(userId);

        HashMap<String, DataModel> map = new HashMap<>();

        for(EachData data : allData){
            map.put(data.getId()+"",data.getModel());
        }

        ref.setValue(map).addOnCompleteListener(task -> {
            if(task.isComplete()){
                listener.onBackupProcessed(null);
            }
            else{
                listener.onBackupProcessed("Failed to backup data. Try again later.");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }
    //for showing search and more icon in top above

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.barLogout){
            new AlertDialog.Builder(this)
                    .setTitle("LogOut?")
                    .setMessage(R.string.are_you_sure_you_want_to_log_out)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                        logOutUser();
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isSheetShowing) {
            filterViewModel.setShowOrHide(false);
        } else {
            if (isDoubleClickDone) {
                finishAffinity();
            } else {
                isDoubleClickDone = true;
                showSafeToast(getString(R.string.click_again_to_exit));
                new Handler(Looper.getMainLooper()).postDelayed(() -> isDoubleClickDone = false, 2000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
