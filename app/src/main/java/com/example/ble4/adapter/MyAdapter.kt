package com.example.ble4.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ble4.databinding.RvItemBinding

class MyAdapter: RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: RvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding=RvItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
//        return differ.currentList.size
//        return bleList.size
        return bles.size
    }


    //lets implement the diff Util callback here
    private val differCallback = object : DiffUtil.ItemCallback<BluetoothDevice>(){
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address==newItem.address
        }

        override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem==newItem
        }

    }
    val differ= AsyncListDiffer(this,differCallback)
    var bles:List<BluetoothDevice>
        get()=differ.currentList
        set(value){differ.submitList(value)}
    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        val curr=differ.currentList[position]
//        val curr=bleList[position]
        val curr=bles[position]
        holder.binding.apply {
            tvDeviceName.text=curr.name
            if(tvDeviceName.text.isEmpty())tvDeviceName.text="UnKnown"
            tvAddress.text=curr.address
            //later we will apply onclick listener here
            holder.itemView.setOnClickListener {
                onItemClickListener?.let {
                    it(curr)
                }
            }
        }
    }
    private var onItemClickListener:((BluetoothDevice)->Unit)?=null
    fun setOnItemClickListener(listener: (BluetoothDevice)->Unit){
        onItemClickListener=listener
    }
}