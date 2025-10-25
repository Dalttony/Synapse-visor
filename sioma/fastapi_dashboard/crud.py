
from sqlalchemy.orm import Session
from . import models, schemas
import json
from datetime import datetime


def create_attendance(db: Session, attendance: schemas.AttendanceCreate):
    
    db_attendance = models.Attendance(
        worker_name=attendance.workerName,
        entry_date=datetime.fromtimestamp(attendance.entryDate / 1000),  # Convertir de milisegundos
        exit_date=datetime.fromtimestamp(attendance.exitDate / 1000) if attendance.exitDate else None,
    )
    db.add(db_attendance)
    db.commit()
    db.refresh(db_attendance)
    return db_attendance

def parse_payload_json(payload_json_str: str) -> schemas.PayloadData:
   
    try:
        payload_dict = json.loads(payload_json_str)
        return schemas.PayloadData(**payload_dict)
    except json.JSONDecodeError as e:
        raise ValueError(f"Error al parsear JSON: {str(e)}")