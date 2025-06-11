package fcu.app.appclassfinalproject.main_fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.helper.SqlDataBaseHelper;
import java.util.ArrayList;
import java.util.List;

public class AddFragment extends Fragment {

  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  private static final String TAG = "AddFragment";

  private String mParam1;
  private String mParam2;

  public AddFragment() {
    // Required empty public constructor
  }

  private TextView etPName;
  private TextView etPSummary;
  private AutoCompleteTextView actvPM;
  private Button btnADD;
  private Button btnAddMember;
  private RecyclerView rvSelectedMembers;
  private SqlDataBaseHelper sqlDataBaseHelper;
  private SQLiteDatabase db;

  private String[] accountList = new String[]{};
  private ArrayAdapter<String> adapter;
  private List<String> selectedMembers;
  private SelectedMembersAdapter selectedMembersAdapter;

  public static AddFragment newInstance(String param1, String param2) {
    AddFragment fragment = new AddFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
    selectedMembers = new ArrayList<>();
    Log.d(TAG, "onCreate: Fragment created and selectedMembers initialized");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Log.d(TAG, "=== onCreateView START ===");

    View view = inflater.inflate(R.layout.fragment_add, container, false);
    Log.d(TAG, "Layout inflated successfully");

    // 強制初始化，確保所有步驟都執行
    if (!initializeDatabase()) {
      Log.e(TAG, "Database initialization failed - returning early");
      return view;
    }

    if (!initializeViews(view)) {
      Log.e(TAG, "Views initialization failed - returning early");
      return view;
    }

    if (!setupData()) {
      Log.e(TAG, "Data setup failed - returning early");
      return view;
    }

    setupEventListeners();

    Log.d(TAG, "=== onCreateView COMPLETED ===");
    return view;
  }

  private boolean initializeDatabase() {
    try {
      Log.d(TAG, "Initializing database...");
      sqlDataBaseHelper = new SqlDataBaseHelper(this.getContext());
      db = sqlDataBaseHelper.getWritableDatabase();
      Log.d(TAG, "Database initialized successfully");
      return true;
    } catch (Exception e) {
      Log.e(TAG, "Database initialization failed: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean initializeViews(View view) {
    Log.d(TAG, "Initializing views...");

    try {
      etPName = view.findViewById(R.id.et_ProjectName);
      etPSummary = view.findViewById(R.id.et_ProjectSummary);
      actvPM = view.findViewById(R.id.actv_PM);
      btnADD = view.findViewById(R.id.btn_addProject);
      btnAddMember = view.findViewById(R.id.btn_addManager);
      rvSelectedMembers = view.findViewById(R.id.rv_selectedManagers);

      // 檢查每個 UI 元件
      boolean allViewsFound = true;
      if (etPName == null) {
        Log.e(TAG, "etPName (R.id.et_ProjectName) not found!");
        allViewsFound = false;
      }
      if (etPSummary == null) {
        Log.e(TAG, "etPSummary (R.id.et_ProjectSummary) not found!");
        allViewsFound = false;
      }
      if (actvPM == null) {
        Log.e(TAG, "actvPM (R.id.actv_PM) not found!");
        allViewsFound = false;
      }
      if (btnADD == null) {
        Log.e(TAG, "btnADD (R.id.btn_addProject) not found!");
        allViewsFound = false;
      }
      if (btnAddMember == null) {
        Log.e(TAG, "btnAddMember (R.id.btn_addManager) not found!");
        allViewsFound = false;
      }
      if (rvSelectedMembers == null) {
        Log.e(TAG, "rvSelectedMembers (R.id.rv_selectedManagers) not found!");
        allViewsFound = false;
      }

      if (!allViewsFound) {
        Toast.makeText(getContext(), "介面元件載入失敗，請檢查佈局檔案", Toast.LENGTH_LONG).show();
        return false;
      }

      Log.d(TAG, "All views found successfully");
      return true;
    } catch (Exception e) {
      Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean setupData() {
    Log.d(TAG, "Setting up data...");

    try {
      // 獲取帳號列表
      accountList = getAccountList();
      Log.d(TAG, "Loaded " + accountList.length + " accounts");

      if (accountList.length == 0) {
        Log.w(TAG, "No accounts found in database");
      } else {
        Log.d(TAG, "Available accounts: " + java.util.Arrays.toString(accountList));
      }

      // 設定 AutoCompleteTextView 的適配器
      adapter = new ArrayAdapter<>(this.requireContext(),
          android.R.layout.simple_dropdown_item_1line, accountList);
      actvPM.setAdapter(adapter);
      Log.d(TAG, "AutoCompleteTextView adapter set");

      // 設定已選擇成員的 RecyclerView
      selectedMembersAdapter = new SelectedMembersAdapter(selectedMembers,
          new SelectedMembersAdapter.OnMemberRemoveListener() {
            @Override
            public void onRemove(String member) {
              Log.d(TAG, "Removing member: " + member);
              selectedMembers.remove(member);
              selectedMembersAdapter.notifyDataSetChanged();
              updateAddMemberButtonState();
              Toast.makeText(getContext(), "已移除成員：" + member, Toast.LENGTH_SHORT).show();
            }
          });

      rvSelectedMembers.setLayoutManager(new LinearLayoutManager(getContext()));
      rvSelectedMembers.setAdapter(selectedMembersAdapter);
      Log.d(TAG, "RecyclerView adapter set");

      return true;
    } catch (Exception e) {
      Log.e(TAG, "Error setting up data: " + e.getMessage(), e);
      Toast.makeText(getContext(), "資料設定失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
      return false;
    }
  }

  private void setupEventListeners() {
    Log.d(TAG, "Setting up event listeners...");

    try {
      // AutoCompleteTextView 文字變化監聽器
      actvPM.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          Log.d(TAG, "Text changed: '" + s.toString() + "'");
          updateAddMemberButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
      });

      // 新增成員按鈕點擊事件
      btnAddMember.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Log.d(TAG, "===== ADD MEMBER BUTTON CLICKED =====");
          handleAddMemberClick();
        }
      });

      // 建立專案按鈕點擊事件
      btnADD.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Log.d(TAG, "===== ADD PROJECT BUTTON CLICKED =====");
          handleAddProjectClick();
        }
      });

      // 初始化按鈕狀態
      updateAddMemberButtonState();
      Log.d(TAG, "Event listeners set up successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error setting up event listeners: " + e.getMessage(), e);
    }
  }

  private void handleAddMemberClick() {
    Log.d(TAG, "handleAddMemberClick() called");

    String selectedAccount = actvPM.getText().toString().trim();
    Log.d(TAG, "Selected account: '" + selectedAccount + "'");

    if (selectedAccount.isEmpty()) {
      Log.d(TAG, "Selected account is empty");
      Toast.makeText(getContext(), "請先選擇或輸入一個成員帳號", Toast.LENGTH_SHORT).show();
      return;
    }

    // 檢查帳號是否存在
    Log.d(TAG, "Checking if account exists: " + selectedAccount);
    if (!isAccountExist(selectedAccount)) {
      Log.d(TAG, "Account does not exist: " + selectedAccount);
      Toast.makeText(getContext(), "找不到帳號「" + selectedAccount + "」，請確認帳號是否正確",
          Toast.LENGTH_LONG).show();
      return;
    }

    Log.d(TAG, "Account exists: " + selectedAccount);

    // 檢查是否已經選擇過
    if (selectedMembers.contains(selectedAccount)) {
      Log.d(TAG, "Member already selected: " + selectedAccount);
      Toast.makeText(getContext(), "成員「" + selectedAccount + "」已經被選擇", Toast.LENGTH_SHORT)
          .show();
      return;
    }

    // 新增成員到列表
    selectedMembers.add(selectedAccount);
    Log.d(TAG, "Member added successfully. Total members: " + selectedMembers.size());
    Log.d(TAG, "Current member list: " + selectedMembers.toString());

    // 更新 UI
    selectedMembersAdapter.notifyDataSetChanged();
    actvPM.setText(""); // 清空輸入框
    updateAddMemberButtonState();

    Toast.makeText(getContext(), "已新增成員：" + selectedAccount, Toast.LENGTH_SHORT).show();
    Log.d(TAG, "Add member operation completed successfully");
  }

  private void handleAddProjectClick() {
    Log.d(TAG, "handleAddProjectClick() called");

    String name = etPName.getText().toString().trim();
    String summary = etPSummary.getText().toString().trim();

    Log.d(TAG, "Project name: '" + name + "'");
    Log.d(TAG, "Project summary: '" + summary + "'");
    Log.d(TAG, "Selected members count: " + selectedMembers.size());
    Log.d(TAG, "Selected members: " + selectedMembers.toString());

    if (name.isEmpty()) {
      Toast.makeText(getContext(), "請輸入專案名稱", Toast.LENGTH_SHORT).show();
      return;
    }

    if (summary.isEmpty()) {
      Toast.makeText(getContext(), "請輸入專案摘要", Toast.LENGTH_SHORT).show();
      return;
    }

    if (selectedMembers.isEmpty()) {
      Toast.makeText(getContext(), "請至少選擇一個專案成員", Toast.LENGTH_SHORT).show();
      return;
    }

    // 建立專案
    ContentValues projectValues = new ContentValues();
    projectValues.put("name", name);
    projectValues.put("summary", summary);

    long projectId = db.insert("Projects", null, projectValues);
    Log.d(TAG, "Project insert result: " + projectId);

    if (projectId != -1) {
      // 新增專案成員關聯
      boolean allMembersAdded = true;
      for (String memberAccount : selectedMembers) {
        int memberId = getUserIdByAccount(memberAccount);
        Log.d(TAG, "Adding member: " + memberAccount + " with ID: " + memberId);

        if (memberId != -1) {
          ContentValues userProjectValues = new ContentValues();
          userProjectValues.put("user_id", memberId);
          userProjectValues.put("project_id", (int) projectId);

          long linkResult = db.insert("UserProject", null, userProjectValues);
          Log.d(TAG, "UserProject insert result for " + memberAccount + ": " + linkResult);

          if (linkResult == -1) {
            allMembersAdded = false;
            Log.e(TAG, "Failed to add member: " + memberAccount);
          }
        } else {
          allMembersAdded = false;
          Log.e(TAG, "Member not found: " + memberAccount);
        }
      }

      if (allMembersAdded) {
        Toast.makeText(getContext(), "專案「" + name + "」建立成功！", Toast.LENGTH_SHORT).show();
        clearForm();
        Log.d(TAG, "Project created successfully and form cleared");
      } else {
        Toast.makeText(getContext(), "專案建立成功，但部分成員新增失敗", Toast.LENGTH_SHORT).show();
      }
    } else {
      Toast.makeText(getContext(), "專案建立失敗，請重試", Toast.LENGTH_SHORT).show();
      Log.e(TAG, "Failed to insert project");
    }
  }

  private void clearForm() {
    etPName.setText("");
    etPSummary.setText("");
    actvPM.setText("");
    selectedMembers.clear();
    selectedMembersAdapter.notifyDataSetChanged();
    updateAddMemberButtonState();
    Log.d(TAG, "Form cleared");
  }

  private boolean isAccountExist(String account) {
    Cursor cursor = null;
    try {
      cursor = db.rawQuery("SELECT id FROM Users WHERE account = ? COLLATE NOCASE",
          new String[]{account});
      boolean exists = cursor.moveToFirst();
      Log.d(TAG, "Account '" + account + "' exists: " + exists);
      return exists;
    } catch (Exception e) {
      Log.e(TAG, "Error checking account existence: " + e.getMessage());
      return false;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private int getUserIdByAccount(String account) {
    Cursor cursor = null;
    try {
      cursor = db.rawQuery("SELECT id FROM Users WHERE account = ? COLLATE NOCASE",
          new String[]{account});
      int userId = -1;
      if (cursor.moveToFirst()) {
        userId = cursor.getInt(0);
      }
      Log.d(TAG, "User ID for account '" + account + "': " + userId);
      return userId;
    } catch (Exception e) {
      Log.e(TAG, "Error getting user ID: " + e.getMessage());
      return -1;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private void updateAddMemberButtonState() {
    if (btnAddMember == null) {
      Log.w(TAG, "btnAddMember is null, cannot update state");
      return;
    }

    String currentText = actvPM != null ? actvPM.getText().toString().trim() : "";
    boolean shouldEnable = !currentText.isEmpty();

    btnAddMember.setEnabled(shouldEnable);

    // 視覺回饋
    btnAddMember.setAlpha(shouldEnable ? 1.0f : 0.5f);

    Log.d(TAG, "Button state updated - Enabled: " + shouldEnable + ", Text: '" + currentText
        + "', Button object: " + btnAddMember);
  }

  public String[] getAccountList() {
    List<String> accountList = new ArrayList<>();
    final String SQL = "SELECT account FROM Users ORDER BY account";

    Cursor cursor = null;
    try {
      SQLiteDatabase readDb = sqlDataBaseHelper.getReadableDatabase();
      cursor = readDb.rawQuery(SQL, null);

      while (cursor.moveToNext()) {
        String account = cursor.getString(0);
        if (account != null && !account.trim().isEmpty()) {
          accountList.add(account.trim());
        }
      }
      Log.d(TAG, "Loaded " + accountList.size() + " accounts from database");
    } catch (Exception e) {
      Log.e(TAG, "Error loading account list: " + e.getMessage());
      Toast.makeText(getContext(), "載入用戶列表時發生錯誤: " + e.getMessage(), Toast.LENGTH_SHORT)
          .show();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return accountList.toArray(new String[0]);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (db != null && db.isOpen()) {
      db.close();
      Log.d(TAG, "Database connection closed");
    }
  }

  // 已選擇成員的適配器
  public static class SelectedMembersAdapter extends
      RecyclerView.Adapter<SelectedMembersAdapter.ViewHolder> {

    private static final String TAG = "SelectedMembersAdapter";
    private List<String> members;
    private OnMemberRemoveListener listener;

    public interface OnMemberRemoveListener {

      void onRemove(String member);
    }

    public SelectedMembersAdapter(List<String> members, OnMemberRemoveListener listener) {
      this.members = members;
      this.listener = listener;
      Log.d(TAG, "Adapter created with " + members.size() + " members");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      Log.d(TAG, "Creating view holder");
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_selected_manager, parent, false);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      String member = members.get(position);
      Log.d(TAG, "Binding member at position " + position + ": " + member);
      holder.tvMemberName.setText(member);
      holder.btnRemove.setOnClickListener(v -> {
        Log.d(TAG, "Remove button clicked for member: " + member);
        if (listener != null) {
          listener.onRemove(member);
        }
      });
    }

    @Override
    public int getItemCount() {
      int count = members.size();
      Log.d(TAG, "getItemCount: " + count);
      return count;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

      TextView tvMemberName;
      Button btnRemove;

      public ViewHolder(View itemView) {
        super(itemView);
        tvMemberName = itemView.findViewById(R.id.tv_manager_name);
        btnRemove = itemView.findViewById(R.id.btn_remove_manager);

        if (tvMemberName == null) {
          Log.e(TAG, "tv_manager_name not found in layout!");
        }
        if (btnRemove == null) {
          Log.e(TAG, "btn_remove_manager not found in layout!");
        }
      }
    }
  }
}