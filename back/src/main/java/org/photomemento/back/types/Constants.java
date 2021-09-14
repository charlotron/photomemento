package org.photomemento.back.types;

import org.photomemento.back.exceptions.InvalidStateError;

import java.io.File;

public class Constants {

    private Constants() {
        throw new InvalidStateError("This is a constants class should not be instantiated");
    }

    // --------- CHARS RELATED ------------
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String EMPTY_STRING = "";
    public static final String WHITESPACE = " ";
    public static final String SEP_STR_LIST = COMMA;
    public static final String SEP_STR_LIST2 = ";";
    public static final String DEGREE_CHAR = "°";
    public static final String SLASH = "/";
    public static final String FILE_EXT_SEPARATOR = DOT;
    public static final String FILE_DIR_SEPARATOR = File.separator;
    public static final String ROUTE_PARAM_START = "{";
    public static final String ROUTE_PARAM_END = "}";
    public static final String MINUS = "-";

    // --------- ROUTES RELATED ------------
    // Params
    public static final String ID = "id";
    public static final String QUERY = "query";
    public static final String ZOOM = "zoom";
    public static final String ID_PARAM = ROUTE_PARAM_START + ID + ROUTE_PARAM_END;
    public static final String QUERY_PARAM = ROUTE_PARAM_START + QUERY + ROUTE_PARAM_END;
    public static final String ZOOM_PARAM = ROUTE_PARAM_START + ZOOM + ROUTE_PARAM_END;

    // Absolute urls (Controller urls)
    public static final String ABS_ROOT = SLASH;
    public static final String ABS_API = "/api/";

    public static final String ABS_MANAGE = ABS_API + "manage/";
    public static final String ABS_STATS = ABS_API + "stats/";
    public static final String ABS_MEDIA = ABS_API + "media/";
    public static final String ABS_PING = ABS_API + "ping/";
    public static final String ABS_LOCATION = ABS_API + "location/";

    public static final String ABS_MEDIA_IMG = ABS_MEDIA + "image/";
    public static final String ABS_MEDIA_VID = ABS_MEDIA + "video/";
    public static final String ABS_MEDIA_DIR = ABS_MEDIA + "directory/";
    public static final String ABS_SEARCH_DIR = ABS_MEDIA + "search/";
    public static final String ABS_GEO_DATA_DIR = ABS_MEDIA + "geodata/";

    // Relative urls (Controller method urls)
    public static final String REL_BY_ID = SLASH + ID_PARAM + SLASH;
    public static final String REL_QUERY = SLASH + QUERY_PARAM + SLASH;
    public static final String REL_DIR_CHILDS = REL_BY_ID + "childs/";
    public static final String REL_DIR_MEDIA = REL_BY_ID + "media/";
    public static final String REL_DIR_REPROCESS = REL_BY_ID + "reprocess/";
    public static final String REL_MANAGE_RECHECK = SLASH + "recheck/";
    public static final String REL_MANAGE_REPROCESS = SLASH + "reprocess/";
    public static final String REL_MANAGE_CHECK_INTEGRITY = SLASH + "check-integrity/";
    public static final String REL_SEARCH_BY_QUERY = SLASH + REL_QUERY;
    public static final String REL_GEO_DATA_BY_ZOOM = SLASH + ZOOM_PARAM;
    public static final String REL_LOCATION_BY_ZOOM = REL_GEO_DATA_BY_ZOOM;

    // --------- MEDIA SUFFIX RELATED ------------
    //Image sizes
    public static final String MINI_SUFFIX = "m";
    public static final String NORMAL_SUFFIX = "n";
    public static final String ORIGINAL_SUFFIX = "o";
    //Video sizes
    public static final String V_MINI_SUFFIX = "vm";
    public static final String V_NORMAL_SUFFIX = "vn";
    public static final String V_ORIGINAL_SUFFIX = "vo";

    // --------- FIELDS RELATED ------------
    public static final String CITY = "city";
    public static final String GPS_LATITUDE = "GPS Latitude";
    public static final String GPS_LONGITUDE = "GPS Longitude";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String ORIENTATION = "Orientation";
    public static final String ROTATION = "Rotation";
    public static final String ORIENTATION_ROTATED = "rotate";
    public static final int ORIENTATION_ROTATED_90_DEGREES = 90;
    public static final String IMAGE_WIDTH = "Image Width";
    public static final String IMAGE_HEIGHT = "Image Height";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    public static final String DURATION = "Duration";
    public static final String DATE_TIME = "date/time";
    public static final String DETECTED_MIME_TYPE = "Detected MIME Type";

    // --------- MESSAGES RELATED ------------
    public static final String COULD_NOT_FIND_PROPERTY_S_ON_METADATA_FOR_FILE_S = "Could not find property \"%s\" on metadata for file %s";

    //---------- VIDEO STREAM RELATED -----------
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String ACCEPT_RANGES = "Accept-Ranges";
    public static final String BYTES = "bytes";

    // --------- OTHER RELATED ------------
    public static final String RESAMPLED_MEDIA_FILE_FORMATTED = "%s" + Constants.FILE_DIR_SEPARATOR + "%s_%s.%s";


    public static final String FF_STREAM = "FF Stream %s";
    public static final String FF_FORMAT = "FF Format";
}
