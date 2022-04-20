package jp.techacademy.wenglon.leong.autoslideshowapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import android.provider.MediaStore
import android.content.ContentUris
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() , View.OnClickListener{
    private val PERMISSIONS_REQUEST_CODE = 100

    private var index:Int = 0
    private var id:Long = -1
    private var idList: MutableList<Long> = mutableListOf()

    private var mTimer: Timer? = null
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }


        prev_button.setOnClickListener(this)
        play_button.setOnClickListener(this)
        next_button.setOnClickListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                }
        }
    }

    private fun getContentsInfo() {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        if (cursor!!.moveToFirst()) {
            do {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
            idList.add(id)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                
                imageView.setImageURI(imageUri)
        } while (cursor.moveToNext())
            }
            cursor.close()

        }

    override fun onClick(v: View?) {
        if(idList.size == 1){
            if (v != null) {
                Snackbar.make(v, "画像が1つしかありません", Snackbar.LENGTH_SHORT)
                    .show()
            }
        } else {
            if (v != null) {
                when(v.id){
                    R.id.next_button -> getNextImage()
                    R.id.play_button -> playSlideShow()
                    R.id.prev_button -> getPrevImage()
                }
            }
        }
    }
    private fun playSlideShow(){
        if (mTimer == null){
            // 再生ボタンのテキストを変更し、進むボタンと戻るボタンを非活性にする
            play_button.text = "停止"
            prev_button.isEnabled  = false
            next_button.isEnabled  = false
            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mHandler.post {
                        getNextImage()
                    }
                }
            }, 2000, 2000) // 最初に始動させるまで 100ミリ秒、ループの間隔を 100ミリ秒 に設定
        } else {
            // 停止ボタンのテキストを変更し、進むボタンと戻るボタンを活性にする
            play_button.text = "再生"
            prev_button.isEnabled  = true
            next_button.isEnabled  = true
            if (mTimer != null){
                mTimer!!.cancel()
                mTimer = null
            }
        }
    }

    private fun getNextImage() {
        // 次の画像を取得する
        if(index + 1 == idList.size){
            index = 0
        } else {
            index++
        }
        setImageId()
    }

    private fun getPrevImage() {
        // 前の画像を取得する
        if(index  == 0){
            index = idList.size - 1
        } else {
            index--
        }
        setImageId()
    }

    private fun setImageId() {
        //indexで指定されている画像を表示
        id = idList[index]
        imageView.setImageURI(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id))
    }

}







