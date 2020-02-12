package com.tmstudios.paperboard.resources;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import com.tmstudios.paperboard.layers.*;


public class VideoResource
{

	private static final String TAG = "VideoResource";
	private MediaPlayer mediaPlayer;

	private OnVideoStartedListener onVideoStartedListener;
	private OnVideoEndedListener onVideoEndedListener;

	private VideoLayer mVideoLayer;

	private boolean isSurfaceCreated;
	private boolean isDataSourceSet;

	private PlayerState state = PlayerState.NOT_PREPARED;

	private Context mContext;

	float videoAspectRatio=1.0f;

	public VideoResource(Context context)
	{
		this.mContext = context;
		initMediaPlayer();
	}


	private void initMediaPlayer()
	{
		mediaPlayer = new MediaPlayer();
		setLooping(true);

		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp)
				{
					state = PlayerState.PAUSED;
					if (onVideoEndedListener != null)
					{
						onVideoEndedListener.onVideoEnded();
					}
				}
			});
	}


	public void setVideoLayer(VideoLayer mVideoLayer)
	{
		if (isDataSourceSet)
		{
			mVideoLayer.setAspectRatio(videoAspectRatio);
		}
		mVideoLayer.setOnSurfacePrepareListener(new VideoLayer.OnSurfacePrepareListener() {
				@Override
				public void onSurfacePrepared(Surface surface)
				{
					isSurfaceCreated = true;
					mediaPlayer.setSurface(surface);
					surface.release();
					if (isDataSourceSet)
					{
						prepareAndStartMediaPlayer();
					}
				}
			});
	}

	private void prepareAndStartMediaPlayer()
	{
		prepareAsync(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp)
				{
					start();
				}
			});
	}

	private void calculateVideoAspectRatio(int videoWidth, int videoHeight)
	{
		if (videoWidth > 0 && videoHeight > 0)
		{
			videoAspectRatio = (float) videoWidth / videoHeight;
		}
		if (mVideoLayer != null)
			mVideoLayer.setAspectRatio(videoAspectRatio);
	}

	private void onDataSourceSet(MediaMetadataRetriever retriever)
	{
		int videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
		int videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

		calculateVideoAspectRatio(videoWidth, videoHeight);
		isDataSourceSet = true;

		if (isSurfaceCreated)
		{
			prepareAndStartMediaPlayer();
		}
	}

	public void setVideoFromAssets(String assetsFileName)
	{
		reset();

		try
		{
			AssetFileDescriptor assetFileDescriptor = mContext.getAssets().openFd(assetsFileName);
			mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());

			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());

			onDataSourceSet(retriever);

		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public void setVideoByUrl(String url)
	{
		reset();

		try
		{
			mediaPlayer.setDataSource(url);

			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(url, new HashMap<String, String>());

			onDataSourceSet(retriever);

		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public void setVideoFromFile(FileDescriptor fileDescriptor)
	{
		reset();

		try
		{
			mediaPlayer.setDataSource(fileDescriptor);

			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(fileDescriptor);

			onDataSourceSet(retriever);

		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public void setVideoFromFile(FileDescriptor fileDescriptor, int startOffset, int endOffset)
	{
		reset();

		try
		{
			mediaPlayer.setDataSource(fileDescriptor, startOffset, endOffset);

			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(fileDescriptor, startOffset, endOffset);

			onDataSourceSet(retriever);

		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	@TargetApi(23)
	public void setVideoFromMediaDataSource(MediaDataSource mediaDataSource)
	{
		reset();

		mediaPlayer.setDataSource(mediaDataSource);

		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(mediaDataSource);

		onDataSourceSet(retriever);
	}

	public void setVideoFromUri(Context context, Uri uri)
	{
		reset();

		try
		{
			mediaPlayer.setDataSource(context, uri);

			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(context, uri);

			onDataSourceSet(retriever);
		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}




	private void prepareAsync(final MediaPlayer.OnPreparedListener onPreparedListener)
	{
		if (mediaPlayer != null && state == PlayerState.NOT_PREPARED
			|| state == PlayerState.STOPPED)
		{
			mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp)
					{
						state = PlayerState.PREPARED;
						onPreparedListener.onPrepared(mp);
					}
				});
			mediaPlayer.prepareAsync();
		}
	}

	public void start()
	{
		if (mediaPlayer != null)
		{
			switch (state)
			{
				case PREPARED:
					mediaPlayer.start();
					state = PlayerState.STARTED;
					if (onVideoStartedListener != null)
					{
						onVideoStartedListener.onVideoStarted();
					}
					break;
				case PAUSED:
					mediaPlayer.start();
					state = PlayerState.STARTED;
					break;
				case STOPPED:
					prepareAsync(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp)
							{
								mediaPlayer.start();
								state = PlayerState.STARTED;
								if (onVideoStartedListener != null)
								{
									onVideoStartedListener.onVideoStarted();
								}
							}
						});
					break;
			}
		}
	}

	public void pause()
	{
		if (mediaPlayer != null && state == PlayerState.STARTED)
		{
			mediaPlayer.pause();
			state = PlayerState.PAUSED;
		}
	}

	public void stop()
	{
		if (mediaPlayer != null && (state == PlayerState.STARTED || state == PlayerState.PAUSED))
		{
			mediaPlayer.stop();
			state = PlayerState.STOPPED;
		}
	}

	public void reset()
	{
		if (mediaPlayer != null && (state == PlayerState.STARTED || state == PlayerState.PAUSED ||
			state == PlayerState.STOPPED))
		{
			mediaPlayer.reset();
			state = PlayerState.NOT_PREPARED;
		}
	}

	public void release()
	{
		if (mediaPlayer != null)
		{
			mediaPlayer.release();
			state = PlayerState.RELEASE;
		}
	}

	public PlayerState getState()
	{
		return state;
	}

	public boolean isPlaying()
	{
		return state == PlayerState.STARTED;
	}

	public boolean isPaused()
	{
		return state == PlayerState.PAUSED;
	}

	public boolean isStopped()
	{
		return state == PlayerState.STOPPED;
	}

	public boolean isReleased()
	{
		return state == PlayerState.RELEASE;
	}

	public void seekTo(int msec)
	{
		mediaPlayer.seekTo(msec);
	}

	public void setLooping(boolean looping)
	{
		mediaPlayer.setLooping(looping);
	}

    public int getCurrentPosition()
	{
        return mediaPlayer.getCurrentPosition();
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener onErrorListener)
	{
        mediaPlayer.setOnErrorListener(onErrorListener);
    }

    public void setOnVideoStartedListener(OnVideoStartedListener onVideoStartedListener)
	{
        this.onVideoStartedListener = onVideoStartedListener;
    }

    public void setOnVideoEndedListener(OnVideoEndedListener onVideoEndedListener)
	{
        this.onVideoEndedListener = onVideoEndedListener;
    }

    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener onSeekCompleteListener)
	{
        mediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
    }

    public MediaPlayer getMediaPlayer()
	{
        return mediaPlayer;
    }

    public interface OnVideoStartedListener
	{
        void onVideoStarted();
    }

    public interface OnVideoEndedListener
	{
        void onVideoEnded();
    }

    private enum PlayerState
	{
        NOT_PREPARED, PREPARED, STARTED, PAUSED, STOPPED, RELEASE
		}
}
