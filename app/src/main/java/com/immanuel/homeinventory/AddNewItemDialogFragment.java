package com.immanuel.homeinventory;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by immanuel on 10/1/17.
 */

public class AddNewItemDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface AddNewItemDialogListener {
        public void onDialogAddClick(String newItemID, String newItemName);
    }

    // Use this instance of the interface to deliver action events
    AddNewItemDialogListener mListener;
    public String newItemName;
    public String newItemID;

    // Override the Fragment.onAttach() method to instantiate the listener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the listener so we can send events to the host
            mListener = (AddNewItemDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AddNewItemDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        newItemID = getArguments().getString("newItemID");

        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View inflatedView = inflater.inflate(R.layout.add_item_dialog, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflatedView);

        ((TextView) inflatedView.findViewById(R.id.newItemID)).setText(
                getResources().getString(R.string.add_item_message, newItemID)
        );

        builder.setTitle(R.string.dialog_add_item_title)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        newItemName = ((EditText) inflatedView.findViewById(R.id.newItemName)).getText().toString();
                        mListener.onDialogAddClick(newItemID, newItemName);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
