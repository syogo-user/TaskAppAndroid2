package jp.techacademy.shogo.taskapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context:Context):BaseAdapter() {

    private val mLayoutInflater:LayoutInflater

    var taskList = mutableListOf<Task>()


    init{
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    //アイテム（データ）の数を返す
    override fun getCount(): Int {
        return taskList.size
    }

    //アイテム（データ）を返す
    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    //アイテム（データ）のIDを返す

    override fun getItemId(position: Int): Long {
        return taskList[position].id.toLong()
    }

    //アイテム（データ）のviewを返す
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view : View = convertView ?: mLayoutInflater.inflate(R.layout.list_layout,null)
        val textView1 = view.findViewById<TextView>(R.id.textView1)
        val textView2 = view.findViewById<TextView>(R.id.textView2)
        val textView3 = view.findViewById<TextView>(R.id.textView3)

        //あとでTaskクラスから情報を取得するように変更する
        textView1.text = taskList[position].title
        //カテゴリ
        textView2.text = taskList[position].category

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
        val date = taskList[position].date
        textView3.text = simpleDateFormat.format(date)
        return view
    }
}