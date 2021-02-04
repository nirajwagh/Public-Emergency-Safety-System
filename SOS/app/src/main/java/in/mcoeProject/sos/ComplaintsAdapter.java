//Adapter for the user complaints recycler view.

package in.mcoeProject.sos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ComplaintsAdapter extends RecyclerView.Adapter<ComplaintsAdapter.viewHolder> {

    //Declaring a list for assigning the passed complaint list.
    private List<ComplaintsDataClass> complaintsDataClass;

    //Declaring a context for assigning the passed context.
    private Context context;

    public ComplaintsAdapter(List<ComplaintsDataClass> complaintsDataClass, Context context) {
        this.complaintsDataClass = complaintsDataClass;
        this.context = context;
    }

    //View holder for inflating the recycler view items.
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());

        //Inflating the complaints_card.xml layout file.
        View view=layoutInflater.inflate(R.layout.complaints_cards, parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {

        //Setting the data for the recycler item views.
        holder.txt_complaint_no.setText(complaintsDataClass.get(i).getComplaintId());
        holder.txt_complaint_date2.setText(complaintsDataClass.get(i).getComplaintCreatedDate());
        holder.txt_complaint_time2.setText(complaintsDataClass.get(i).getComplaintCreatedTime());
        holder.txt_cop_name.setText(complaintsDataClass.get(i).getCopName());
        holder.txt_cop_id_no.setText(complaintsDataClass.get(i).getCopId());
        holder.txt_phone.setText(complaintsDataClass.get(i).getCopPhone());
        String profile_image_url=complaintsDataClass.get(i).getCopProfileUrl();

        //Setting the profile image using Glide library.
        if( profile_image_url!=null){
            Glide.with(context).load(profile_image_url).into(holder.img_cop_profile1);
        }
    }

    @Override
    public int getItemCount() {
        return complaintsDataClass.size();
    }

    public class  viewHolder extends RecyclerView.ViewHolder{

        TextView txt_complaint_no, txt_complaint_date2, txt_complaint_time2, txt_cop_name, txt_cop_id_no, txt_phone;
        ImageView img_cop_profile1;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            //Obtaining the views of item of recycler view.
            txt_complaint_date2=itemView.findViewById(R.id.txt_complaint_date2);
            txt_complaint_no=itemView.findViewById(R.id.txt_complaint_no);
            txt_complaint_time2=itemView.findViewById(R.id.txt_complaint_time2);
            txt_cop_name=itemView.findViewById(R.id.txt_cop_name);
            txt_cop_id_no=itemView.findViewById(R.id.txt_cop_id_no);
            txt_phone=itemView.findViewById(R.id.txt_phone);
            img_cop_profile1=itemView.findViewById(R.id.img_cop_profile1);
        }
    }
}
