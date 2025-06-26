package ch.zeitmessungen.equestre.adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.zeitmessungen.equestre.R
import ch.zeitmessungen.equestre.data.models.ConsumerModel
import java.text.SimpleDateFormat
import java.util.*

class SportsEventAdapter(
    private val items: List<ConsumerModel>,
    private val onItemClick: (ConsumerModel) -> Unit
) : RecyclerView.Adapter<SportsEventAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvEventInfo: TextView = view.findViewById(R.id.tvEventInfo)
        val tvEventDate: TextView = view.findViewById(R.id.tvEventDate)
        val tvCategoryCountry: TextView = view.findViewById(R.id.tvCategoryCountry)
        val tvLive: TextView = view.findViewById(R.id.tvLive)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sports_event, parent, false)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = items[position]
        holder.tvTitle.text = model.info?.title ?: "-"
        holder.tvEventInfo.text = "${model.info?.eventTitle ?: "-"} - ${model.info?.eventTime ?: "-"}"
        holder.tvEventDate.text = formatDate(model.info?.eventDate)
        holder.tvCategoryCountry.text = "${model.info?.category ?: "-"} - ${model.info?.country ?: "-"}"
        holder.tvLive.visibility = if (model.info?.live == true) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onItemClick(model) }
    }

    private fun formatDate(date: Date?): String {
        return date?.let {
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(it)
        } ?: "-"
    }
}