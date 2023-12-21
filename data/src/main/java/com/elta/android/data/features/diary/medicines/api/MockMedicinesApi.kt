package com.elta.android.data.features.diary.medicines.api

import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse
import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import com.google.gson.Gson
import io.reactivex.Single

class MockMedicinesApi : MedicinesApi {

    override fun getMedicaments(touchedAfter: Long?): Single<List<MedicamentNetworkResponse>> {
        return Single.fromCallable { generateMedicament(20) }
    }

    private fun generateMedicament(count: Int): List<MedicamentNetworkResponse> {
        val list = mutableListOf<MedicamentNetworkResponse>()
        repeat(count) {
            list.add(
                MedicamentNetworkResponse(
                    id = (it + 1).toLong(),
                    name = "Medicament $it",
                    deleted = false,
                    other = it == 0,
                    touchedAt = it.toLong()
                )
            )
        }
        return list
    }

    override fun getInsulinMedicines(): Single<InsulinMedicamentsNetworkResponse> {
        val json = """
    
    {
   "insulinMedicamentsByType":{
      "ULTRA_SHORT":[
         {
            "id":1,
            "name":"Фиасп",
            "insulinType":{
               "code":"ULTRA_SHORT",
               "name":"Ультракороткий",
               "id":1
            }
         },
         {
            "id":56,
            "name":"Другое",
            "insulinType":{
               "code":"ULTRA_SHORT",
               "name":"Ультракороткий",
               "id":1
            }
         }
      ],
      "PROLONGED":[
         {
            "id":21,
            "name":"Протафан HM",
            "insulinType":{
               "code":"PROLONGED",
               "name":"Продлённый",
               "id":8
            }
         },
         {
            "id":32,
            "name":"РинГлар",
            "insulinType":{
               "code":"PROLONGED",
               "name":"Продлённый",
               "id":8
            }
         },
         {
            "id":54,
            "name":"Другое",
            "insulinType":{
               "code":"PROLONGED",
               "name":"Продлённый",
               "id":8
            }
         }
      ],
      "MIXED":[
         {
            "id":37,
            "name":"Хумулин М3",
            "insulinType":{
               "code":"MIXED",
               "name":"Смешанный",
               "id":6
            }
         },
         {
            "id":49,
            "name":"Райзодег",
            "insulinType":{
               "code":"MIXED",
               "name":"Смешанный",
               "id":6
            }
         },
         {
            "id":55,
            "name":"Другое",
            "insulinType":{
               "code":"MIXED",
               "name":"Смешанный",
               "id":6
            }
         }
      ]
   },
   "basalInsulinTypes":[
      "PROLONGED"
   ],
   "bolusInsulinTypes":[
      "ULTRA_SHORT"
   ]
}
    
    """


        return Single.fromCallable {
            Gson().fromJson(
                json,
                InsulinMedicamentsNetworkResponse::class.java
            )
        }
    }

}




