#include <pebble.h>

#define SAMPLE_BATCH 18

static Window *window;
static TextLayer *text_layer;

DataLoggingSessionRef accel_log;

typedef struct {
    //bool did_vibrate;
    //uint64_t start_timestamp;
    //uint64_t end_timestamp;
    int x_avg;
    int y_avg;
    //int z_avg;
    int num_samples;
} AccelDataAvg;

AccelDataAvg current_avg;
char time_formatted[6];

void init_dlog(void) {
   accel_log = data_logging_create(
     /* tag */           0xacce7,
     /* DataLogType */ DATA_LOGGING_BYTE_ARRAY,
     /* length */        sizeof(AccelData) * SAMPLE_BATCH,
     /* resume */        false );
}

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Select");
}

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Up");
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Down");
}

static void accel_data_handler(AccelData *data, uint32_t num_samples) {
    if (num_samples >= SAMPLE_BATCH) {
        DataLoggingResult r = data_logging_log(accel_log, data, 1);
    }
}

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

static void handle_tick(struct tm *tick_time, TimeUnits units_changed) {
    snprintf(time_formatted, 6, "%02d:%02d", tick_time->tm_hour, tick_time->tm_min);
    text_layer_set_text(text_layer, time_formatted);
}

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  text_layer = text_layer_create((GRect) { .origin = { 0, 62 }, .size = { bounds.size.w, 40 } });
  text_layer_set_font(text_layer, fonts_get_system_font(FONT_KEY_BITHAM_30_BLACK));

  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
}

static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
}

static void init(void) {
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);
  tick_timer_service_subscribe(MINUTE_UNIT, &handle_tick);
  accel_data_service_subscribe(SAMPLE_BATCH, &accel_data_handler);
  accel_service_set_sampling_rate(ACCEL_SAMPLING_10HZ);
  init_dlog();
}

static void deinit(void) {
  data_logging_finish(accel_log);
  accel_data_service_unsubscribe();
  tick_timer_service_unsubscribe();
  window_destroy(window);
}

int main(void) {
  init();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", window);
  APP_LOG(APP_LOG_LEVEL_DEBUG, "Started: %lu", time(NULL));


  app_event_loop();
  deinit();
}
