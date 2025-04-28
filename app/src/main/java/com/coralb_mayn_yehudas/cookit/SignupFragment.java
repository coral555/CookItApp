package com.coralb_mayn_yehudas.cookit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SignupFragment extends Fragment {

    public SignupFragment() {
        // חובה קונסטרקטור ריק
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // מחזיר את התצוגה של טופס ההרשמה
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }
}
