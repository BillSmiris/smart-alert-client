package org.unipi.mpsp2343.smartalert.recyclers.reportRecycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.unipi.mpsp2343.smartalert.Helpers;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.dto.ReportDto;
import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

//Data adapter for the recycler that shows the reports of the event in the event details activity
public class ReportRecyclerViewAdapter extends RecyclerView.Adapter<ReportRecyclerViewAdapter.ReportViewHolder>{
    private List<ReportDto> reports;
    private Context context;

    //Class for the items of the recycler
    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        private final TextView email; //the email of the user that made the report
        private final TextView comments; //The comments that the user included in their report
        private final ImageView photo; //The photo that the user included in their report

        public ReportViewHolder(View view) {
            super(view);
            email = (TextView) view.findViewById(R.id.email);
            comments = (TextView) view.findViewById(R.id.comments);
            photo = (ImageView) view.findViewById(R.id.photo);
        }

        public TextView getEmail() {
            return email;
        }

        public TextView getComments() {
            return comments;
        }

        public ImageView getPhoto() {
            return photo;
        }
    }

    public ReportRecyclerViewAdapter(List<ReportDto> reports, Context context) {
        this.reports = reports;
        this.context = context;
    }

    @Override
    public ReportRecyclerViewAdapter.ReportViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.report, viewGroup, false);

        return new ReportRecyclerViewAdapter.ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReportRecyclerViewAdapter.ReportViewHolder viewHolder, final int position) {
        ReportDto report = reports.get(position);
        //Displays the emails and the comments as is and converts the base64 received image to bitmap,
        //for display in an image view.
        viewHolder.getEmail().setText(report.getUserEmail());
        viewHolder.getComments().setText(report.getComments());
        viewHolder.getPhoto().setImageBitmap(Helpers.base64ToBitmap(report.getPhotoBase64()));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

}
