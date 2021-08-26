/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.sample.cast.castconnect;

import java.util.ArrayList;
import java.util.List;

public final class MovieList {
    public static final String MOVIE_CATEGORY[] = {
            "Videos",
    };

    private static List<Movie> list;
    private static long count = 0;

    public static List<Movie> getList() {
        if (list == null) {
            list = setupMovies();
        }
        return list;
    }

    public static List<Movie> setupMovies() {
        list = new ArrayList<>();
        String title[] = {
                "Designing For Google Cast",
                "Casting To The Future",
                "Making Cast Ready Apps Discoverable",
                "Big Buck Bunny",
                "Elephants Dream"
        };

        String description = "Fusce id nisi turpis. Praesent viverra bibendum semper. "
                + "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est "
                + "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit "
                + "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit "
                + "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id "
                + "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.";
        String studio[] = {
                "Google IO - 2014", "Google IO - 2014", "Google IO - 2014", "Blender Foundation", "Blender Foundation"
        };
        String videoUrl[] = {
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/DesigningForGoogleCast.m3u8",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/GoogleIO-2014-CastingToTheFuture.m3u8",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/GoogleIO-2014-MakingGoogleCastReadyAppsDiscoverable.m3u8",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/BigBuckBunny.m3u8",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/ElephantsDream.m3u8"
        };
        String bgImageUrl[] = {
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/DesigningForGoogleCast2-480x270.jpg",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/ToTheFuture2-480x270.jpg",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/MakingGoogleCastReadyAppsDiscoverable-480-270.jpg",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/BigBuckBunny.jpg",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/ElephantsDream.jpg",
        };
        String cardImageUrl[] = {
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/DesigningForGoogleCast2-480x270.jpg",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/ToTheFuture2-480x270.jpg",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/MakingGoogleCastReadyAppsDiscoverable-480-270.jpg",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/BigBuckBunny.jpg",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/ElephantsDream.jpg"
        };

        for (int index = 0; index < title.length; ++index) {
            list.add(
                    buildMovieInfo(
                            title[index],
                            description,
                            studio[index],
                            videoUrl[index],
                            cardImageUrl[index],
                            bgImageUrl[index]));
        }

        return list;
    }

    private static Movie buildMovieInfo(
            String title,
            String description,
            String studio,
            String videoUrl,
            String cardImageUrl,
            String backgroundImageUrl) {
        Movie movie = new Movie();
        movie.setId(count++);
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setStudio(studio);
        movie.setCardImageUrl(cardImageUrl);
        movie.setBackgroundImageUrl(backgroundImageUrl);
        movie.setVideoUrl(videoUrl);
        return movie;
    }
}