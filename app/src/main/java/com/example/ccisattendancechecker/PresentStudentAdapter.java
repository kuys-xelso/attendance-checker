package com.example.ccisattendancechecker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PresentStudentAdapter extends  RecyclerView.Adapter<PresentStudentAdapter.StudentViewHolder>{
    List<StudentNameClass> studentList;

    public PresentStudentAdapter( List<StudentNameClass> studentList) {

        this.studentList = studentList;

    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.name_present, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {

        StudentNameClass studentName = studentList.get(position);
        holder.studentName.setText(studentName.getStudentName());
        holder.statusTv.setText(studentName.getStatus());
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder{
        TextView studentName,statusTv;;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            studentName = itemView.findViewById(R.id.studentName);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }

}
