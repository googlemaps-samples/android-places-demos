/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.libraries.places.samples.kotlinview

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.samples.kotlinview.core.DemoCategory
import com.google.android.libraries.places.samples.kotlinview.core.DemoItem
import com.google.android.libraries.places.samples.kotlinview.core.DemoRegistry
import com.google.android.libraries.places.samples.kotlinview.databinding.ActivityMainBinding
import com.google.android.libraries.places.samples.kotlinview.databinding.ItemDemoCardBinding
import com.google.android.libraries.places.samples.kotlinview.databinding.ItemDemoCategoryBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DemoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = DemoAdapter { item ->
            startActivity(Intent(this, item.activityClass))
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        adapter.submitDemos(DemoRegistry.getDemos())

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.submitDemos(DemoRegistry.searchDemos(s?.toString().orEmpty()))
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    sealed class ListItem {
        data class CategoryHeader(val category: DemoCategory) : ListItem()
        data class DemoCard(val demoItem: DemoItem) : ListItem()
    }

    class DemoAdapter(private val onItemClick: (DemoItem) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val items = mutableListOf<ListItem>()

        fun submitDemos(demos: List<DemoItem>) {
            items.clear()
            var currentCategory: DemoCategory? = null
            demos.forEach { demo ->
                if (demo.category != currentCategory) {
                    val cat = demo.category
                    currentCategory = cat
                    items.add(ListItem.CategoryHeader(cat))
                }
                items.add(ListItem.DemoCard(demo))
            }
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is ListItem.CategoryHeader -> 0
                is ListItem.DemoCard -> 1
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == 0) {
                val binding = ItemDemoCategoryBinding.inflate(inflater, parent, false)
                CategoryViewHolder(binding)
            } else {
                val binding = ItemDemoCardBinding.inflate(inflater, parent, false)
                DemoViewHolder(binding, onItemClick)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is ListItem.CategoryHeader -> (holder as CategoryViewHolder).bind(item.category)
                is ListItem.DemoCard -> (holder as DemoViewHolder).bind(item.demoItem)
            }
        }

        override fun getItemCount(): Int = items.size

        class CategoryViewHolder(private val binding: ItemDemoCategoryBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(category: DemoCategory) {
                binding.categoryTitle.text = category.title
                binding.categoryDescription.text = category.description
            }
        }

        class DemoViewHolder(
            private val binding: ItemDemoCardBinding,
            private val onItemClick: (DemoItem) -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(demoItem: DemoItem) {
                binding.demoTitle.text = demoItem.title
                binding.demoDescription.text = demoItem.description
                binding.root.setOnClickListener { onItemClick(demoItem) }
            }
        }
    }
}
