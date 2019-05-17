package com.decoders.demoapp

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

class HomeAdapter(private val mContext: Context, private val mList: ArrayList<DataModel>) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater? = null

    init {
        mLayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mList.size
    }

    override fun getItem(pos: Int): Any {
        return mList[pos]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val viewHolder: CompleteListViewHolder
        if (convertView == null) {
            val li = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = li.inflate(R.layout.home_item, null)
            viewHolder = CompleteListViewHolder(v)
            v!!.tag = viewHolder
        } else {
            viewHolder = v!!.tag as CompleteListViewHolder
        }

        val model = mList[position]

        // set views data here
        viewHolder.note!!.text = model.note
        viewHolder.image!!.setImageBitmap(BitmapFactory.decodeFile(model.imagePath))
        return v
    }

    private class CompleteListViewHolder(view:View){
        var image: ImageView = view.findViewById(R.id.image)
        var note: TextView = view.findViewById(R.id.note)
    }
}
