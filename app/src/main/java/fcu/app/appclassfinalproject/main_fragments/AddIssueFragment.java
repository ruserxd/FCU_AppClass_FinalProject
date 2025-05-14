package fcu.app.appclassfinalproject.main_fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import fcu.app.appclassfinalproject.CreateIssueActivity;
import fcu.app.appclassfinalproject.ProjectActivity;
import fcu.app.appclassfinalproject.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddIssueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddIssueFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText etPurpose;
    private EditText etOverview;
    private EditText etStartTime;
    private EditText etEndTime;
    private EditText etStatus;
    private EditText etDesignee;

    private ImageButton btnSave;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public AddIssueFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddIssueFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddIssueFragment newInstance(String param1, String param2) {
        AddIssueFragment fragment = new AddIssueFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_issue, container, false);

        FirebaseApp.initializeApp(getContext());
        mAuth = FirebaseAuth.getInstance();

        //找對應id
        etPurpose = view.findViewById(R.id.et_purpose);
        etOverview = view.findViewById(R.id.et_overview);
        etStartTime = view.findViewById(R.id.et_start_time);
        etEndTime = view.findViewById(R.id.et_endtime);
        etStatus = view.findViewById(R.id.et_status);
        etDesignee = view.findViewById(R.id.et_designee);

        btnSave = view.findViewById(R.id.btn_save);


        // 假設 sharedPreference 有　"projectId"
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = etPurpose.getText().toString().trim();
                String summary = etOverview.getText().toString().trim();
                String start_time = etStartTime.getText().toString().trim();
                String end_time = etEndTime.getText().toString().trim();
//                String status = etStatus.getText().toString().trim();
//                String designee = etDesignee.getText().toString().trim();


                Map<String, Object> issue = new HashMap<>();
//                issue.put("id",id);
                issue.put("name", name);
                issue.put("summary", summary);
                issue.put("start_time", start_time);
                issue.put("end_time", end_time);
//                issue.put("project_id",projectId);
//                issue.put("status",status);
//                issue.put("designee",designee);
                db.collection("issues").add(issue).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "建立成功，ID:", Toast.LENGTH_LONG).show();
                        clearFields();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "建立失敗，請檢查輸入是否正確", Toast.LENGTH_LONG).show();
                    }
                });


            }
        });


        return view;
    }
    private void clearFields() {
        etPurpose.setText("");
        etOverview.setText("");
        etStartTime.setText("");
        etEndTime.setText("");
    }
}