package me.kbai.remotetoolkit.ui.ping

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.kbai.remotetoolkit.databinding.ItemRecordBinding

/**
 * @author sean 2022/11/24
 */
class PingRecordAdapter : RecyclerView.Adapter<PingRecordAdapter.WolRecordHolder>() {
    var onItemClickListener: ((String) -> Unit)? = null
    var onItemRemoveClickListener: ((adapter: PingRecordAdapter, index: Int) -> Unit)? = null

    var records: MutableList<String> = ArrayList()
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
        val host = records[position]

        holder.binding.run {
            root.setOnClickListener {
                onItemClickListener?.invoke(host)
            }
            tvAlias.text = host
            remove.setOnClickListener {
                onItemRemoveClickListener?.invoke(this@PingRecordAdapter, holder.adapterPosition)
            }
        }
    }

    override fun getItemCount() = records.size
}