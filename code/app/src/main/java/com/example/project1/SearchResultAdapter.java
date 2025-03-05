package com.example.project1;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder> {

    private List<User> userList;
    private Context context;

    public SearchResultAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView displayNameText;
        TextView usernameText;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            displayNameText = itemView.findViewById(R.id.text_display_name);
            usernameText = itemView.findViewById(R.id.text_username);

            // Set an OnClickListener to open the user's profile when tapped
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    User clickedUser = userList.get(position);
                    openUserProfile(clickedUser);
                }
            });
        }

        private void openUserProfile(User user) {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("USER_ID", user.getId());
            intent.putExtra("name", user.getDisplayName());
            intent.putExtra("username", user.getUsername());
            context.startActivity(intent);
        }
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        User user = userList.get(position);
        holder.displayNameText.setText(user.getDisplayName());
        holder.usernameText.setText(user.getUsername());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OtherProfileActivity.class);
            intent.putExtra("USER_ID", user.getId());  // ✅ Ensure the correct user ID is passed
            context.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return userList.size();
    }
}