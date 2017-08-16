package com.example.wenhai.listenall.moudle.artist.detail

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.example.wenhai.listenall.R
import com.example.wenhai.listenall.data.bean.Album
import com.example.wenhai.listenall.data.bean.Artist
import com.example.wenhai.listenall.data.bean.Song
import com.example.wenhai.listenall.moudle.detail.DetailFragment
import com.example.wenhai.listenall.moudle.main.MainActivity
import com.example.wenhai.listenall.utils.FragmentUtil
import com.example.wenhai.listenall.utils.GlideApp
import com.example.wenhai.listenall.utils.ToastUtil

class ArtistDetailFragment : Fragment(), ArtistDetailContract.View {

    @BindView(R.id.detail_pager)
    lateinit var mPager: ViewPager
    @BindView(R.id.detail_pager_tab)
    lateinit var mTab: TabLayout
    @BindView(R.id.detail_artist_name)
    lateinit var mArtistName: TextView
    @BindView(R.id.detail_artist_photo)
    lateinit var mArtistPhoto: ImageView


    //歌手热门歌曲
    lateinit var mHotSongsView: LinearLayout
    lateinit var mHotSongList: RecyclerView

    //歌手专辑
    lateinit var mAlbumsView: LinearLayout
    lateinit var mAlbumList: RecyclerView

    //歌手详情
    lateinit var mArtistInfoView: LinearLayout
    lateinit var mArtistDesc: TextView

    lateinit var mUnbinder: Unbinder
    lateinit var mPresenter: ArtistDetailContract.Presenter
    lateinit var artist: Artist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArtistDetailPresenter(this)
        artist = arguments.getParcelable("artist")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater !!.inflate(R.layout.fragment_artist_detail, container, false)

        mHotSongsView = inflater.inflate(R.layout.fragment_artist_detail_hot_songs, container, false) as LinearLayout
        mHotSongList = mHotSongsView.findViewById(R.id.detail_song_list)

        mAlbumsView = inflater.inflate(R.layout.fragment_artist_detail_albums, container, false) as LinearLayout
        mAlbumList = mAlbumsView.findViewById(R.id.detail_album_list)

        mArtistInfoView = inflater.inflate(R.layout.fragment_artist_detail_info, container, false) as LinearLayout
        mArtistDesc = mArtistInfoView.findViewById(R.id.detail_artist_desc)
        mUnbinder = ButterKnife.bind(this, contentView)
        initView()
        return contentView
    }

    override fun initView() {
        mPager.adapter = DetailPagerAdapter()
        mTab.setupWithViewPager(mPager)
        mPresenter.loadArtistDetail(artist)
        mArtistName.text = artist.name
        mArtistPhoto.setOnClickListener { }

        mPresenter.loadArtistHotSongs(artist)
        mPresenter.loadArtistAlbums(artist)
    }

    @OnClick(R.id.action_bar_back)
    fun onClick(view: View) {
        when (view.id) {
            R.id.action_bar_back -> {
                FragmentUtil.removeFragment(fragmentManager, this)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mUnbinder.unbind()
    }

    override fun setPresenter(presenter: ArtistDetailContract.Presenter) {
        mPresenter = presenter
    }

    override fun onFailure(msg: String) {
        activity.runOnUiThread {
            ToastUtil.showToast(context, msg)
        }
    }

    override fun onArtistDetail(artist: Artist) {
        activity.runOnUiThread {
            GlideApp.with(this).load(artist.imgUrl)
                    .placeholder(R.drawable.ic_main_singer)
                    .into(mArtistPhoto)
            mArtistDesc.text = artist.desc
        }
    }

    override fun onHotSongsLoad(hotSongs: List<Song>) {
        activity.runOnUiThread {
            mHotSongList.layoutManager = LinearLayoutManager(context)
            mHotSongList.adapter = HotSongsAdapter(context, hotSongs)
        }
    }

    override fun onAlbumsLoad(albums: List<Album>) {
        activity.runOnUiThread {
            mAlbumList.adapter = AlbumAdapter(context, albums)
            mAlbumList.layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onSongDetailLoaded(song: Song) {
        (activity as MainActivity).playNewSong(song)
    }


    inner class DetailPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup?, position: Int): Any {
            val pageView = when (position) {
                0 -> {
                    mHotSongsView
                }
                1 -> {
                    mAlbumsView
                }
                2 -> {
                    mArtistInfoView
                }
                else -> {
                    mHotSongsView
                }
            }
            container !!.addView(pageView, position)
            return pageView
        }

        override fun getItemPosition(`object`: Any?): Int {
            return super.getItemPosition(`object`)
        }

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
            container !!.removeViewAt(position)
//            super.destroyItem(container, position, `object`)
        }

        override fun getPageTitle(position: Int): CharSequence {
            val title = when (position) {
                0 -> {
                    "热门歌曲"
                }
                1 -> {
                    "专辑"
                }
                2 -> {
                    "艺人详情"
                }
                else -> {
                    ""
                }
            }
            return title
        }

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean = view == `object`

        override fun getCount(): Int = 3

    }

    inner class HotSongsAdapter(val context: Context, var hotSongs: List<Song>) : RecyclerView.Adapter<HotSongsAdapter.ViewHolder>() {

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val song = hotSongs[position]
            val index = "${position + 1}"
            holder !!.index.text = index
            holder.title.text = song.name
            // 虾米没有专辑信息
            if (song.albumName != "") {
                holder.album.visibility = View.VISIBLE
                holder.album.text = song.albumName
            } else {
                holder.album.visibility = View.GONE
            }
            holder.item.setOnClickListener({
                mPresenter.loadSongDetail(song)
            })
        }

        override fun getItemCount(): Int = hotSongs.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_artist_detail_song_list, parent, false)
            return ViewHolder(itemView)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var item: LinearLayout = itemView.findViewById(R.id.detail_song_item)
            var index: TextView = itemView.findViewById(R.id.detail_index)
            val title: TextView = itemView.findViewById(R.id.detail_song_title)
            var album: TextView = itemView.findViewById(R.id.detail_album)
        }
    }

    inner class AlbumAdapter(val context: Context, var albums: List<Album>) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_artist_detail_album, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val album = albums[position]
            GlideApp.with(context).load(album.miniCoverUrl)
                    .placeholder(R.drawable.ic_main_all_music)
                    .into(holder !!.albumCover)
            holder.albumName.text = album.title
            val publishDate = "发行时间:${album.publishDateStr}"
            holder.albumPublishDate.text = publishDate
            holder.item.setOnClickListener {
                val detailFragment = DetailFragment()
                val data = Bundle()
                data.putLong(DetailFragment.ARGS_ID, album.id)
                data.putInt(DetailFragment.ARGS_TYPE, DetailFragment.TYPE_ALBUM)
                detailFragment.arguments = data
                FragmentUtil.addFragmentToMainView(fragmentManager, detailFragment)

            }
        }

        override fun getItemCount(): Int = albums.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.detail_album_cover)
            val albumName: TextView = itemView.findViewById(R.id.detail_album_name)
            val albumPublishDate: TextView = itemView.findViewById(R.id.detail_album_publish_date)
            val item: LinearLayout = itemView.findViewById(R.id.detail_album_item)
        }
    }

}