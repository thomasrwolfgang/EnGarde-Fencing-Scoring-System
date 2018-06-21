#include <stdint.h>
#include <stdbool.h>
#include "ble.h"
#include "ble_srv_common.h"
#include "boards.h"
#include "nrf_gpio.h"

//UUID: 544da9d9-53c0-4416-a1f8-ed418de6a65a

#define ENGARDE_SERVICE_UUID_BASE		{0x5A, 0xA6, 0xE6, 0x8D, 0x41, 0xED, 0xF8, 0xA1, \
									 	 0x16, 0x44, 0xC0, 0x53, 0xD9, 0xA9, 0x4D, 0x54}
#define ENGARDE_SERVICE_UUID 			0x1400
#define ENGARDE_HITSTATUS_CHAR_UUID		0x1401
#define ENGARDE_HITTIME_CHAR_UUID		0x1402							 	 

/**@brief   Macro for defining a ble_egs instance.
 *
 * @param   _name   Name of the instance.
 * @hideinitializer
 */
#define BLE_EGS_DEF(_name)        	\
static ble_egs_t _name;  			\
NRF_SDH_BLE_OBSERVER(_name ## _obs, BLE_HRS_BLE_OBSERVER_PRIO, ble_egs_on_ble_evt, &_name)


//Types of EnGarde Service Events
typedef enum
{
    BLE_EGS_EVT_DISCONNECTED,
    BLE_EGS_EVT_CONNECTED,
    BLE_EGS_EVT_NOTIFICATION_ENABLED,
    BLE_EGS_EVT_NOTIFICATION_DISABLED,
    BLE_EGS_EVT_HITDETECTED
} ble_egs_evt_type_t;


/**@brief EnGarde Service event. */
typedef struct
{
    ble_egs_evt_type_t evt_type;                                  /**< Type of event. */
} ble_egs_evt_t;

// Forward declaration of the ble_egs_t type.
typedef struct ble_egs_s ble_egs_t;

/**@brief EnGarde Service event handler type. */
typedef void (*ble_egs_evt_handler_t) (ble_egs_t * p_egs, ble_egs_evt_t * p_evt);


/**@brief EnGarde Service init structure. This contains all options and data needed for
 *        initialization of the service.*/
typedef struct
{
	ble_egs_evt_handler_t		  evt_handler;					  /**< Event handler to be called for handling events in the EnGarde Service. */
    uint8_t                       initial_custom_value;           /**< Initial custom value */
    ble_srv_cccd_security_mode_t  hitstatus_value_char_attr_md;   /**< Initial security level for HitStatus characteristics attribute */
    ble_srv_cccd_security_mode_t  hittime_value_char_attr_md;	  /**< Initial security level for HitTime characteristics attribute */
} ble_egs_init_t;

/**@brief Engarde Service structure. This contains various status information for the service. */
struct ble_egs_s
{
	ble_egs_evt_handler_t		  evt_handler;					  /**< Event handler to be called for handling events in the EnGarde Service. */
    uint16_t                      service_handle;                 /**< Handle of Engarde Service (as provided by the BLE stack). */
    ble_gatts_char_handles_t      hitstatus_value_handles;        /**< Handles related to the hitstatus Value characteristic. */
    ble_gatts_char_handles_t	  hittime_value_handles;
    uint16_t                      conn_handle;                    /**< Handle of the current connection (as provided by the BLE stack, is BLE_CONN_HANDLE_INVALID if not in a connection). */
    uint8_t                       uuid_type; 
};






/**@brief Function for initializing the EnGarde Service.
 *
 * @param[out]  p_cus       EnGarde Service structure. This structure will have to be supplied by
 *                          the application. It will be initialized by this function, and will later
 *                          be used to identify this particular service instance.
 * @param[in]   p_cus_init  Information needed to initialize the service.
 *
 * @return      NRF_SUCCESS on successful initialization of service, otherwise an error code.
 */
uint32_t ble_egs_init(ble_egs_t * p_egs, const ble_egs_init_t * p_egs_init);


/**@brief Function for adding the HitStatus Value characteristic.
 *
 * @param[in]   p_egs        EnGarde Service structure.
 * @param[in]   p_egs_init   Information needed to initialize the service.
 *
 * @return      NRF_SUCCESS on success, otherwise an error code.
 */
uint32_t hitstatus_value_char_add(ble_egs_t * p_egs, const ble_egs_init_t * p_egs_init);

/**@brief Function for adding the HitTime Value characteristic.
 *
 * @param[in]   p_egs        EnGarde Service structure.
 * @param[in]   p_egs_init   Information needed to initialize the service.
 *
 * @return      NRF_SUCCESS on success, otherwise an error code.
 */
uint32_t hittime_value_char_add(ble_egs_t * p_egs, const ble_egs_init_t * p_egs_init);



/**@brief Function for updating the HitStatus value.
 *
 * @details The application calls this function when the hitstatus value should be updated. If
 *          notification has been enabled, the custom value characteristic is sent to the client.
 *
 * @note 
 *       
 * @param[in]   p_egs          Custom Service structure.
 * @param[in]   hitstatus value 
 *
 * @return      NRF_SUCCESS on success, otherwise an error code.
 */

uint32_t ble_egs_hitstatus_value_update(ble_egs_t * p_egs, uint8_t hitstatus_value);

/**@brief Function for updating the HitTime value.
 *
 * @details The application calls this function when the hittime value should be updated.
 *
 * @note 
 *       
 * @param[in]   p_egs          Custom Service structure.
 * @param[in]   hittime value 
 *
 * @return      NRF_SUCCESS on success, otherwise an error code.
 */

uint32_t ble_egs_hittime_value_update(ble_egs_t * p_egs, uint32_t hittime_value); 

/**@brief Function for handling the Application's BLE Stack events.
 *
 * @details Handles all events from the BLE stack of interest to the EnGarde Service.
 *
 * @note 
 *
 * @param[in]   p_ble_evt  Event received from the BLE stack.
 * @param[in]   p_context  Service structure.
 */
void ble_egs_on_ble_evt( ble_evt_t const * p_ble_evt, void * p_context);