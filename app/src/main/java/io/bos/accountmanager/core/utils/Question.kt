package io.bos.accountmanager.core.utils

import android.content.Context
import android.text.TextUtils
import io.bos.accountmanager.R
import io.bos.accountmanager.di.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Question @Inject constructor(@ApplicationContext context: Context) {
    var QUESTIONS_HASH:HashMap<String,QuestionItem> = HashMap()



    init {

//    QUESTIONS_HASH= hashMapOf(
//                Pair("5d863b4ad885541c", QuestionItem( context.getString(R.string.question_txt_mother), "5d863b4ad885541c")),
//                Pair("8906a783203f342d", QuestionItem(context.getString(R.string.question_txt_father), "8906a783203f342d")),
//                Pair("083c384b736517d3", QuestionItem(context.getString(R.string.question_txt_lover), "083c384b736517d3")),
//                Pair("b50f73acff178f79", QuestionItem( context.getString(R.string.question_txt_friend_name), "b50f73acff178f79")),
//                Pair("6bd0447d6aeb5455", QuestionItem(context.getString(R.string.question_txt_son_name), "6bd0447d6aeb5455")),
//                Pair("e59ac9f21816c63f", QuestionItem(context.getString(R.string.question_txt_nickname), "e59ac9f21816c63f")),
//                Pair("ec46070d79755525", QuestionItem(context.getString(R.string.question_txt_pet_name), "ec46070d79755525")),
//                Pair("b80f3f8ee00dfb82", QuestionItem(context.getString(R.string.question_txt_graduation), "b80f3f8ee00dfb82")),
//                Pair("0c606eb37b2afeac", QuestionItem( context.getString(R.string.question_txt_born_place), "0c606eb37b2afeac")),
//                Pair("b11bf1929c4c914f", QuestionItem(context.getString(R.string.question_txt_mother_was_born), "b11bf1929c4c914f")),
//                Pair("7461ae46c81bbfc6", QuestionItem(context.getString(R.string.question_txt_mothers_birthday), "7461ae46c81bbfc6")),
//                Pair("da0ce1afca0a76fe", QuestionItem(context.getString(R.string.question_txt_fathers_birthday) , "da0ce1afca0a76fe")),
//                Pair("9e065595a35ff706", QuestionItem(context.getString(R.string.question_txt_love_birthday), "9e065595a35ff706")),
//                Pair("b1a71ca4905a94fd", QuestionItem(context.getString(R.string.question_txt_friends_birthday), "b1a71ca4905a94fd")),
//                Pair("3acb55eae9c8e9bd", QuestionItem(context.getString(R.string.question_txt_dirst_child), "3acb55eae9c8e9bd")),
//                Pair("772bd405ceb2dc69", QuestionItem(context.getString(R.string.question_txt_second_child), "772bd405ceb2dc69")),
//                Pair("ca6e2a572d3cef31", QuestionItem(context.getString(R.string.question_txt_birthday), "ca6e2a572d3cef31")),
//                Pair("1bfee0b8035384bf", QuestionItem(context.getString(R.string.question_txt_unforgettable), "1bfee0b8035384bf")),
//                Pair("e4aeb36924e07f6a", QuestionItem(context.getString(R.string.question_txt_travel_far), "e4aeb36924e07f6a")),
//                Pair("f37c2034ae7c925b", QuestionItem(context.getString(R.string.question_txt_marry), "f37c2034ae7c925b")),
//                Pair("67cdb073cc980faa", QuestionItem(context.getString(R.string.question_txt_fruits), "67cdb073cc980faa")),
//                Pair("377c08d8837b0384", QuestionItem(context.getString(R.string.question_txt_food), "377c08d8837b0384")),
//                Pair("d15045def02e4925", QuestionItem(context.getString(R.string.question_txt_like_things), "d15045def02e4925")),
//                Pair("ff8a6a0ed55c14b1", QuestionItem(context.getString(R.string.question_txt_what_you_hate_todo), "ff8a6a0ed55c14b1")),
//                Pair("22ab11aeb625c19c", QuestionItem(context.getString(R.string.question_txt_favorite_colours), "22ab11aeb625c19c")),
//                Pair("a2a6208e7021e8cd", QuestionItem(context.getString(R.string.question_txt_motion), "a2a6208e7021e8cd")),
//                Pair("3d6b67f5251e3d0c", QuestionItem(context.getString(R.string.question_txt_city), "3d6b67f5251e3d0c")),
//                Pair("e0fe285d11b8aa88", QuestionItem(context.getString(R.string.question_txt_tourism), "e0fe285d11b8aa88")),
//                Pair("bc7d08e04bb201a0", QuestionItem(context.getString(R.string.question_txt_disgusting), "bc7d08e04bb201a0"))
//
//        )



    }

    fun getSecretProtection(verify:Question.QuestionItem):String{

        return  QUESTIONS_HASH!![verify.id]!!.question

    }

    data class QuestionItem(val question: String, val id: String)

//    /**
//     * 获取三个问题
//     */
//    fun getQuestion(): ArrayList<QuestionItem> {
//        val q1 = QUESTIONS_HASH.keys.elementAt(getQuestionRandom())
//        var q2 = QUESTIONS_HASH.keys.elementAt(getQuestionRandom())
//        while (TextUtils.equals(q2, q1)) {
//            q2 = QUESTIONS_HASH.keys.elementAt(getQuestionRandom())
//        }
//
//        var q3 = QUESTIONS_HASH.keys.elementAt(getQuestionRandom())
//        while (TextUtils.equals(q3, q1) || TextUtils.equals(q3, q2)) {
//            q3 = QUESTIONS_HASH.keys.elementAt(getQuestionRandom())
//        }
//
//        return arrayListOf(QUESTIONS_HASH[q1]!!, QUESTIONS_HASH[q2]!!, QUESTIONS_HASH[q3]!!)
//    }
//
//    /**
//     * 刷新获取一个问题
//     */
//    fun refresh(data: ArrayList<QuestionItem>): QuestionItem {
//        var q = QUESTIONS_HASH.keys.elementAt(getQuestionRandom())
//        while (data.contains(QUESTIONS_HASH[q])) {
//            q = QUESTIONS_HASH.keys.elementAt(getQuestionRandom())
//        }
//        return QUESTIONS_HASH[q]!!
//    }
//
//    private fun getQuestionRandom(): Int {
//        return ((1 + Math.random() * (QUESTIONS_HASH.size - 1 - 1 + 1)).toInt())
//    }
}