import {LatLngTuple} from "leaflet";

export class Constants {

  public static readonly API_PATH = "/api/";

  public static readonly RESOURCES_PATH = "/resources/";
  public static readonly CACHED_MEDIA_PATH = Constants.RESOURCES_PATH + "media/";

  public static readonly MEDIA_PATH = Constants.API_PATH + "media/";

  public static readonly LIST_DIRECTORIES_PATH = Constants.MEDIA_PATH + "directory/";
  public static readonly IMG_PATH = Constants.MEDIA_PATH + "image/";
  public static readonly VID_PATH = Constants.MEDIA_PATH + "video/";
  public static readonly GEO_DATA_PATH = Constants.MEDIA_PATH + "geodata/";
  public static readonly SEARCH_PATH = Constants.MEDIA_PATH + "search/";

  public static readonly LOCATION_PATH = Constants.API_PATH + "location/";

  public static readonly MANAGE_PATH = Constants.API_PATH + "manage/";
  public static readonly RECHECK_FILES_PATH = Constants.MANAGE_PATH + "recheck/";
  public static readonly REPROCESS_FILES_PATH = Constants.MANAGE_PATH + "reprocess/";
  public static readonly CHECK_INTEGRITY_PATH = Constants.MANAGE_PATH + "check-integrity/";

  public static readonly STATS_PATH = Constants.API_PATH + "stats/";

  public static readonly LOCAL_ASSETS = "assets/";
  public static readonly LOCAL_ASSET_IMAGES = Constants.LOCAL_ASSETS + "images/";
  public static readonly LOCAL_DEFAULT_ENV_VARS = Constants.LOCAL_ASSETS+"env.json";
  public static readonly LOCAL_LOCAL_ENV_VARS = Constants.LOCAL_ASSETS+"env.local.json";

  public static readonly URL_PATH_REPARATOR="/";
  public static readonly CHILDS_SUBPATH = "/childs/";
  public static readonly MEDIA_SUBPATH = "/media/";

  public static readonly OTHER = "Other";

  public static readonly KEY_SPACE=" ";
  public static readonly KEY_ESCAPE="Escape";
  public static readonly KEY_ARROW_LEFT="ArrowLeft";
  public static readonly KEY_ARROW_RIGHT="ArrowRight";

  public static readonly  MESSAGE_SHOW_MODAL_TIME: number = 500;
  public static readonly  MESSAGE_HIDE_MODAL_TIME: number = 4000;
  public static readonly  MESSAGE_MAX_NUM:number=3;

  public static readonly  ENV_PROP_BACKEND_BASE_URL="BACKEND_BASE_URL";
  public static readonly  ENV_PROP_GEO_GROUPS="GEO_GROUPS";
  public static readonly  ROOT: string="root";

  public static readonly  STATS_CHECK_INTERVAL: number=5000;

  public static readonly FILE_TYPE_PHOTO: string="PHOTO";
  public static readonly FILE_TYPE_VIDEO: string="VIDEO";
  public static readonly  DEFAULT_COORDINATES: LatLngTuple=[ 40.416775, -3.703790 ];

}
