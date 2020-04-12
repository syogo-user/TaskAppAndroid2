package jp.techacademy.shogo.taskapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_input.*
import kotlinx.android.synthetic.main.activity_input_category.*
import kotlinx.android.synthetic.main.content_input.*
import java.util.*

class InputActivity : AppCompatActivity() {
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask:Task? = null
    private var mCategoryItem = ""


    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener{ _,year,month,dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d",mMonth + 1 ) + "/" +String.format("%02d",mDay)
                date_button.text = dateString
            },mYear,mMonth,mDay)
        datePickerDialog.show()
    }
    private val mOnTimeClickListener = View.OnClickListener{
        val timePickerDialog = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener{_,hour,minute ->
                    mHour = hour
                    mMinute = minute
                    val timeString = String.format("%02d",mHour) + ":" + String.format("%02d",mMinute)
                    times_button.text = timeString
                },mHour,mMinute,false)
        timePickerDialog.show()
    }

    private val mOnDoneClickListerner = View.OnClickListener {
        if(addTask()) {
            //正常終了した場合は画面を閉じる
            finish()
        }
    }

    private val mOnSaveClickListenner = View.OnClickListener {
        val intent = Intent(this@InputActivity,InputCategory::class.java)
        startActivity(intent)
    }

    private val mOnClearClickListener = View.OnClickListener{
        title_edit_text.setText("")
        content_edit_text.setText("")
        category_spinner_text.setSelection(0)

        val calendar = Calendar.getInstance()
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        mHour= calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        val dateString = mYear.toString() + "/" + String.format("%02d",mMonth + 1) + "/" + String.format("%02d",mDay)
        val timeString = String.format("%02d",mHour) + ":" + String.format("%02d",mMinute)

        date_button.text = dateString
        times_button.text = timeString

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        //ActionBar を設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if(supportActionBar != null){
            //戻るボタンの表示
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        //UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        clear_button.setOnClickListener(mOnClearClickListener)
        done_button.setOnClickListener(mOnDoneClickListerner)
        transition_button.setOnClickListener(mOnSaveClickListenner)

        //EXTRA_TASKからTaskのidを取得して、idからTaskのインスタンスを取得する
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK,-1)
        var realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id",taskId).findFirst()
        realm.close()





        //taskIdが-1の時（新規作成時）はmTaskはnullになる
        if(mTask == null){
            //新規作成の場合　現在日時を設定
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour= calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

        }else {
            //更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)


            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d",mMonth + 1) + "/" + String.format("%02d",mDay)
            val timeString = String.format("%02d",mHour) + ":" + String.format("%02d",mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }


    override fun onStart() {
        super.onStart()
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
        category_spinner_text.adapter = adapter


        if (mTask == null) {
            //新規の場合


        }else {
            //更新の場合
            //カテゴリ 表示
            category_spinner_text.setSelection(mTask!!.category!!.categoryId)
        }
        category_spinner_text.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                mCategoryItem = item
            }
            //アイテムが選択されなかった時
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun addTask():Boolean {

        if (mCategoryItem == "") {
            AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                .setTitle("")
                .setMessage("カテゴリを選択してください")
                .setPositiveButton("OK") { _, _ ->

                }
                .show()
            return false
        }
        //mCategoryItemからcategoryIdを取得
        val realmCategory = Realm.getDefaultInstance()
        //val resultCategory = realmCategory.where(Category::class.java).equalTo("categoryName",mCategoryItem).findAll()
        val resultCategory = realmCategory.where(Category::class.java).equalTo("categoryName",mCategoryItem).findFirst()
        realmCategory.close()


        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        if(mTask == null){
            //新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()
            val identifier : Int =
                if(taskRealmResults.max("id") != null){
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
        }
        val title = title_edit_text.text.toString()

        val content = content_edit_text.text.toString()


        mTask!!.title = title
        //val categoryMutableList =  realmCategory.copyToRealm(resultCategory)
        mTask!!.category = resultCategory
        //!!.categoryId  = resultCategory!!.categoryId
        //mTask!!.category!!.categoryName = mCategoryItem
        mTask!!.contents = content
        val calendar  = GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute)
        val date = calendar.time
        mTask!!.date= date

        realm.copyToRealmOrUpdate(mTask!!)
        realm.commitTransaction()

        realm.close()



        var resultIntent = Intent(applicationContext,TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK,mTask!!.id)
        var resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,resultPendingIntent)

        return true
    }

}
