package flooz.android.com.flooz.UI.Fragment.Home;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import flooz.android.com.flooz.Adapter.SelectUserListAdapter;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 10/2/14.
 */
public class TransactionSelectReceiverFragment extends HomeBaseFragment {

    private ImageView backButton;
    private TextView headerTitle;
    private TextView searchTextfield;
    private ImageView clearSearchTextfieldButton;

    private StickyListHeadersListView resultList;

    private SelectUserListAdapter listAdapter;

    public TransactionSelectReceiverDelegate delegate;

    public boolean showCross = false;

    public boolean firstNewFlooz = false;

    public interface TransactionSelectReceiverDelegate {
        public void changeUser(FLUser user);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transaction_select_receiver, null);

        this.backButton = (ImageView) view.findViewById(R.id.transaction_select_receiver_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.transaction_select_receiver_header_text);

        if (showCross)
            this.backButton.setImageResource(R.drawable.nav_cross);
        else
            this.backButton.setImageResource(R.drawable.nav_back);

        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
                InputMethodManager imm = (InputMethodManager) inflater.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            }
        });

        this.searchTextfield = (TextView) view.findViewById(R.id.transaction_select_receiver_search_textfield);
        this.clearSearchTextfieldButton = (ImageView) view.findViewById(R.id.transaction_select_receiver_search_clear);

        this.resultList = (StickyListHeadersListView) view.findViewById(R.id.transaction_select_receiver_result_list);
        this.listAdapter = new SelectUserListAdapter(inflater.getContext());
        this.resultList.setAdapter(this.listAdapter);

        this.resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager) inflater.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

                if (firstNewFlooz) {
                    firstNewFlooz = false;
                    ((NewFloozFragment) parentActivity.contentFragments.get("create")).initWithUser((FLUser)listAdapter.getItem(position));
                    parentActivity.changeMainFragment("create", R.animator.slide_up, android.R.animator.fade_out);
                }
                else {
                    if (delegate != null)
                        delegate.changeUser((FLUser)listAdapter.getItem(position));
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
                }
            }
        });

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.searchTextfield.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        this.searchTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0)
                    clearSearchTextfieldButton.setVisibility(View.VISIBLE);
                else
                    clearSearchTextfieldButton.setVisibility(View.GONE);
                listAdapter.searchUser(editable.toString());
            }
        });

        this.clearSearchTextfieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter.stopSearch();
                searchTextfield.setText("");
                searchTextfield.clearFocus();
                clearSearchTextfieldButton.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) inflater.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        listAdapter.stopSearch();
        searchTextfield.setText("");
        searchTextfield.clearFocus();
        clearSearchTextfieldButton.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        listAdapter.stopSearch();
        searchTextfield.setText("");
        searchTextfield.clearFocus();
        clearSearchTextfieldButton.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        this.backButton.performClick();
    }
}