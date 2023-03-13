package com.example.bluetooth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//Адаптер для Списка
public class StateAdapter extends RecyclerView.Adapter<StateAdapter.ViewHolder>{ interface OnStateClickListener{ void onStateClick(PairedDev pairedDev, int position);
    }
    private static final String TAG = "MyApp";

    private LayoutInflater inflater;
    private ArrayList<PairedDev> pairedDevs;
    private OnStateClickListener onClickListener;
    public StateAdapter(Context context, ArrayList<PairedDev> pairedDevs,OnStateClickListener onClickListener) {
        this.pairedDevs = pairedDevs;
        this.inflater=LayoutInflater.from(context);
        this.onClickListener = onClickListener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageView myImageViev;
        TextView IdTextViev;
        TextView macTextViev;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        textViewName = itemView.findViewById(R.id.nameTextViev);
        macTextViev=itemView.findViewById(R.id.mactextviev);
        myImageViev=itemView.findViewById(R.id.IdimageView);
        IdTextViev=itemView.findViewById(R.id.IdTextViev);
        }
        public TextView getTextView() {
            return textViewName;
        }

    }

    @NonNull
    @Override
    public StateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
       return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StateAdapter.ViewHolder holder, int position) {

        PairedDev pairedDev = pairedDevs.get(position);
        Log.d(TAG, "onBindViewHolder: "+position +" "+pairedDev.devName);
            holder.textViewName.setText(pairedDev.getDevName());
            holder.myImageViev.setImageResource(android.R.drawable.stat_sys_data_bluetooth);
            holder.IdTextViev.setText(Integer.toString(position + 1));
            holder.macTextViev.setText(pairedDev.getMac());

// обработка нажатия
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                // вызываем метод слушателя, передавая ему данные
                onClickListener.onStateClick(pairedDev, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pairedDevs.size();
    }


}
