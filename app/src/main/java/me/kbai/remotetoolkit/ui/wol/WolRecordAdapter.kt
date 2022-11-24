package me.kbai.remotetoolkit.ui.wol

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.kbai.remotetoolkit.databinding.ItemRecordBinding
import me.kbai.remotetoolkit.model.WolRecord

/**
 * @author sean 2022/11/24
 */
class WolRecordAdapter : RecyclerView.Adapter<WolRecordAdapter.WolRecordHolder>() {
    var onItemClickListener: ((WolRecord) -> Unit)? = null
    var onItemRemoveClickListener: ((adapter: WolRecordAdapter, index: Int) -> Unit)? = null

    var records: List<WolRecord>? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class WolRecordHolder(val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = WolRecordHolder(
        ItemRecordBinding.inflate(LayoutInflater.from(parent.context))
    )

    override fun onBindViewHolder(holder: WolRecordHolder, position: Int) {
        val item = records!![position]

        holder.binding.run {
            root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
            tvAlias.text = item.alias.ifBlank { item.host }
            remove.setOnClickListener {
                onItemRemoveClickListener?.invoke(this@WolRecordAdapter, holder.adapterPosition)
            }
        }
    }

    override fun getItemCount() = records?.size ?: 0
}