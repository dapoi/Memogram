package com.dapascript.memogram.presentation.ui.feed

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.paging.*
import androidx.recyclerview.widget.ListUpdateCallback
import com.dapascript.memogram.data.source.UserRepository
import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.presentation.adapter.FeedAdapter
import com.dapascript.memogram.utils.CoroutinesTestRule
import com.dapascript.memogram.utils.DataDummy
import com.dapascript.memogram.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner::class)
class FeedViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var userRepository: UserRepository

    private val token = "token"

    @Test
    fun `Success Get All Stories`() = runTest {
        val dataDummy = DataDummy.dummyFeed()
        val dataPaging = PagedFeedSource.snapshot(dataDummy)
        val feed = MutableLiveData<PagingData<FeedEntity>>()
        feed.value = dataPaging

        val repository = userRepository.getFeed(token)
        `when`(repository).thenReturn(flowOf(feed.value!!))

        val feedViewModel = FeedViewModel(userRepository)
        val actualFeed = feedViewModel.getFeed(token).asLiveData().getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = FeedAdapter.DIFF_UTIL,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualFeed)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(differ.snapshot().size, dataDummy.size)
        Assert.assertEquals(differ.snapshot()[0]?.name, dataDummy[0].name)
    }

    @Test
    fun `Failed Get All Stories`() = runTest {
        val dataDummy = DataDummy.dummyFeed()
        val dataPaging = PagedFeedSource.snapshot(dataDummy)
        val feed = MutableLiveData<PagingData<FeedEntity>>()
        feed.value = dataPaging

        val repository = userRepository.getFeed(token)
        `when`(repository).thenReturn(flowOf(feed.value!!))

        val feedViewModel = FeedViewModel(userRepository)
        val actualFeed = feedViewModel.getFeed(token).asLiveData().getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = FeedAdapter.DIFF_UTIL,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualFeed)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertNotEquals(differ.snapshot().size, dataDummy.size + 1)
        Assert.assertNotEquals(differ.snapshot()[0]?.name, dataDummy[1].name)
    }
}

class PagedFeedSource : PagingSource<Int, LiveData<List<FeedEntity>>>() {
    override fun getRefreshKey(state: PagingState<Int, LiveData<List<FeedEntity>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<FeedEntity>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }

    companion object {
        fun snapshot(items: List<FeedEntity>): PagingData<FeedEntity> {
            return PagingData.from(items)
        }
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}