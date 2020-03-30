package jp.techacademy.shogo.taskapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_input_category.*

class InputCategory : AppCompatActivity() {
    private var mCategory:Category? = null

    private val mOnSaveOnClickListener = View.OnClickListener {
        addCategory()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_category)



        save_button.setOnClickListener(mOnSaveOnClickListener)

    }
    private fun addCategory(){
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        //categoruIdの生成
        val taskRealmResult = realm.where(Category::class.java).findAll()
        val identifier : Int =
            if(taskRealmResult.max("categoryId") !=  null){
                taskRealmResult.max("categoryId")!!.toInt() + 1
            }else {
                0
            }
        //Category生成
        mCategory = Category()
        //categoryIdの設定
        mCategory!!.categoryId = identifier
        //categoryNameの設定
        mCategory!!.categoryName =  category_create_text.text.toString()

        realm.copyToRealmOrUpdate(mCategory!!)
        realm.commitTransaction()
        realm.close()
    }
}
