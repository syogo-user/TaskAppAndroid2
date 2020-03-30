package jp.techacademy.shogo.taskapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class Category : RealmObject() ,Serializable {
    var categoryName :String = ""//カテゴリ名

    @PrimaryKey
    var categoryId : Int = 0 //ID
}