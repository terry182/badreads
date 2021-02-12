/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.timenotclocks.bookcase

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.timenotclocks.bookcase.api.GoodReadImport
import com.timenotclocks.bookcase.database.BooksApplication
import com.timenotclocks.bookcase.database.BookViewModel
import com.timenotclocks.bookcase.database.BookViewModelFactory
import com.timenotclocks.bookcase.ui.main.SectionsPagerAdapter


class MainActivity : AppCompatActivity()  {

    private val goodReadsImport = 2

    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        val fab0: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab0)
        fab0.setOnClickListener {}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                val intent = Intent(applicationContext, SearchActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_import -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                intent.type = "text/*"

                // TODO: add a confirmation
                startActivityForResult(intent, goodReadsImport)
                return true
            }
            R.id.menu_export -> {
                true
            }
            R.id.menu_delete -> {
                Log.i("BK", "Deleting data")
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.apply {
                    setPositiveButton("OK",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User clicked OK button
                                bookViewModel.deleteAll()
                            })
                    setNegativeButton("CANCEL",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User cancelled the dialog
                            })
                }
                builder.setTitle("Delete")
                builder.setMessage("Delete all your data? (This cannot be undone)")
                builder.create()
                builder.show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // TODO: either handler or remove
    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (requestCode == goodReadsImport && resultCode == Activity.RESULT_OK) {

            if (intentData?.data == null) {
                Log.i("BK", "Nothing to report")
                return
            }

            val uri: Uri? = intentData?.data
            if (uri != null) {
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null){
                    val books = GoodReadImport().serialize(inputStream)
                    books.forEach {
                        Log.i("BK", it.toString())
                        bookViewModel.insert(it)
                    }
                }
            }
        }

//        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {
//            intentData?.getStringExtra(SearchActivity.EXTRA_REPLY)?.let { reply ->
//                //val book = Book(reply)
//                //bookViewModel.insert(book)
//            }
//        }

    }
}

