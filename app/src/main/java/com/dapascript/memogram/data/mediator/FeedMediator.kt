package com.dapascript.memogram.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dapascript.memogram.data.source.local.db.FeedDatabase
import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.data.source.local.model.FeedKeys
import com.dapascript.memogram.data.source.remote.network.ApiService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class FeedMediator(
    private val apiService: ApiService,
    private val feedDB: FeedDatabase,
    private val token: String
) : RemoteMediator<Int, FeedEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with endOfPaginationReached = false because Paging
                // will call this method again if TopTVRemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                // the end of pagination for append.
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = apiService.getFeed(
                token = token,
                page = page,
                size = state.config.pageSize
            )
            val feed = response.listStory
            val endOfPaginationReached = feed.isEmpty()

            feedDB.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    feedDB.feedKeysDao().deleteKeys()
                    feedDB.feedDao().deleteFeed()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = feed.map {
                    FeedKeys(it.id, prevKey, nextKey)
                }
                val local = ArrayList<FeedEntity>()
                feed.map { stories ->
                    FeedEntity(
                        stories.id,
                        stories.name,
                        stories.description,
                        stories.photoUrl,
                        stories.createdAt,
                        stories.lat,
                        stories.lon,
                    ).let { local.add(it) }
                }
                feedDB.feedKeysDao().insertKeys(keys)
                feedDB.feedDao().insertFeed(local)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, FeedEntity>): FeedKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            feedDB.feedKeysDao().getKeys(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, FeedEntity>): FeedKeys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            feedDB.feedKeysDao().getKeys(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, FeedEntity>): FeedKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                feedDB.feedKeysDao().getKeys(id)
            }
        }
    }
}