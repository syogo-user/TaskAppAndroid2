package jp.techacademy.shogo.taskapp

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_input.*
import java.util.ArrayList


const val EXTRA_TASK = "jp.techacademy.shogo.taskapp.TASK"

class MainActivity : AppCompatActivity()  {
    private lateinit var mRealm: Realm
    private var  serchItem :MenuItem? = null
    private var serchSpinner :Spinner? = null

    //値が変更されたかを検知するリスナー
    private val mRealmListener = object : RealmChangeListener<Realm>{
        override fun onChange(element:Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter :TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fab.setOnClickListener {
            val intent = Intent(this@MainActivity,InputActivity::class.java)
            startActivity(intent)
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
        }

        //Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        //ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        //ListViewをタップした時の処理
        listView1.setOnItemClickListener{ parent,view,position,id ->
            //入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity,InputActivity::class.java)
            intent.putExtra(EXTRA_TASK,task.id)
            startActivity(intent)
        }

        //ListViewを長押しした時の処理
        listView1.setOnItemLongClickListener { parent, view, position, id ->
            //タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            //ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")
            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id",task.id).findAll()
                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                //アラームを削除
                val resultIntent = Intent(applicationContext,TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)


                reloadListView()
            }

            builder.setNegativeButton("CANCEL",null)
            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()
    }

    private fun reloadListView(){
        //Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults  = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)


        //上記の結果を、TaskListとしてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        //表示を　更新するためにアダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()

    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

    override fun onResume() {
        super.onResume()

        if(serchItem != null) {
            var realm = Realm.getDefaultInstance()
            val mCategory = realm.where(Category::class.java).findAll()
            //ArrayList に型変換
            val sppinnerCategoryArray: Array<Category> =
                realm.copyFromRealm(mCategory).toTypedArray()
            realm.close()

            var sppinnerItemArray: ArrayList<String>? = arrayListOf()
            for (item in sppinnerCategoryArray) {
                sppinnerItemArray?.add(item.categoryName)
            }
            val adapter = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_item,
                sppinnerItemArray
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            serchSpinner!!.adapter = adapter

            serchSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val spinnerParent = parent as Spinner
                    //スピナーで選択した値
                    val item = spinnerParent.selectedItem as String
                    var taskRealmResults: RealmResults<Task?>
                    if (item == "") {
                        taskRealmResults = mRealm.where(Task::class.java).findAll()
                    } else {
                        taskRealmResults =
                            mRealm.where(Task::class.java).equalTo("category.categoryName", item)
                                .findAll()
                    }
                    //上記の結果を、TaskListとしてセットする
                    mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
                    listView1.adapter = mTaskAdapter
                    //表示を　更新するためにアダプターにデータが変更されたことを知らせる
                    mTaskAdapter.notifyDataSetChanged()
                }

                //アイテムが選択されなかった時
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
         menuInflater.inflate(R.menu.option_menu, menu)

        //検索UI
        if(menu != null) {
//            val searchItem = menu.findItem(R.id.search)
//            val searchView = searchItem.actionView as SearchView
             serchItem =  menu.findItem(R.id.search) as MenuItem
             serchSpinner = serchItem!!.actionView as Spinner
            //カテゴリのSpinnerを設定
            //Realからカテゴリを取得
            var realm = Realm.getDefaultInstance()
            val mCategory = realm.where(Category::class.java).findAll()
            //ArrayList に型変換
            val sppinnerCategoryArray: Array<Category> = realm.copyFromRealm(mCategory).toTypedArray()
            realm.close()

            var sppinnerItemArray: ArrayList<String>? = arrayListOf()
            for (item in sppinnerCategoryArray) {
                sppinnerItemArray?.add(item.categoryName)
            }
            val adapter = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_item,
                sppinnerItemArray
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            serchSpinner!!.adapter = adapter

            serchSpinner!!.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val spinnerParent = parent as Spinner
                    //スピナーで選択した値
                    val item = spinnerParent.selectedItem as String
                    var taskRealmResults :RealmResults<Task?>
                    if (item ==""){
                        taskRealmResults = mRealm.where(Task::class.java).findAll()
                    }else {
                        taskRealmResults =
                            mRealm.where(Task::class.java).equalTo("category.categoryName", item)
                                .findAll()
                    }
                    //上記の結果を、TaskListとしてセットする
                    mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
                    listView1.adapter = mTaskAdapter
                    //表示を　更新するためにアダプターにデータが変更されたことを知らせる
                    mTaskAdapter.notifyDataSetChanged()
                }
                //アイテムが選択されなかった時
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        }

        return true
    }



//    private fun addTaskForTest(){
//        val task = Task()
//        task.title = "作業"
//        task.contents = "プログラムを書いてPUSHする"
//        task.date = Date()
//        task.id = 0
//        mRealm.beginTransaction()
//        mRealm.copyToRealmOrUpdate(task)
//        mRealm.commitTransaction()
//
//    }
}
