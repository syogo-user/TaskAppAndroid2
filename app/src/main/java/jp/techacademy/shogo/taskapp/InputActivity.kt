package jp.techacademy.shogo.taskapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ArrayAdapter
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
        addTask()
        finish()
    }

    private val mOnSaveClickListenner = View.OnClickListener {
        val intent = Intent(this@InputActivity,InputCategory::class.java)
        startActivity(intent)
    }

    private val mOnClearClickListener = View.OnClickListener{
        title_edit_text.setText("")
        content_edit_text.setText("")
        //category_edit_text.setText("")

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



        //カテゴリのSpinnerを設定
        //Realからカテゴリを取得
        realm = Realm.getDefaultInstance()
        val mCategory = realm.where(Category::class.java).findAll()
        realm.close()

        //ArrayList に型変換
        val sppinnerCategoryArray :Array<Category> = mCategory.toTypedArray()

        var sppinnerItemArray : ArrayList<String>? = null
        for (item in sppinnerCategoryArray) {
            sppinnerItemArray?.add(item.categoryName)
        }
        val addapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item,
            sppinnerItemArray
            )


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
            //category_edit_text.setText(mTask!!.category.categoryName)

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
    private fun addTask(){
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
        //val category = category_edit_text.text.toString()
        val content = content_edit_text.text.toString()

        mTask!!.title = title
        //mTask!!.category.categoryName = category
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

    }

}
