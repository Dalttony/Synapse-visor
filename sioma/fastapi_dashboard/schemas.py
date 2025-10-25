from pydantic import BaseModel, Field
from typing import Optional

class PayloadData(BaseModel):
    id: Optional[int] = None
    workerName: str
    entryDate: int  # timestamp en milisegundos
    exitDate: Optional[int] = None  # timestamp en milisegundos

class RecognitionCreate(BaseModel):
   
    id: Optional[int] = None
    payloadJson: str  
    createdAt: Optional[int] = None  #
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "payloadJson": '{"id":1,"workerName":"Juan Perez","entryDate":1698765432000,"exitDate":1698776232000}',
                "createdAt": 1698765432000
            }
        }

class AttendanceCreate(BaseModel):
    """Modelo para guardar en la base de datos"""
    workerName: str
    entryDate: int
    exitDate: Optional[int] = None