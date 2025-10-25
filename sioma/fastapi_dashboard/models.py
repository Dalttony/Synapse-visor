from sqlalchemy import Column, Integer, String, DateTime
from sqlalchemy.sql import func  
from .database import Base
from sqlalchemy import Column, Integer, String, DateTime
from datetime import datetime

class Estacion(Base):
    __tablename__ = "estaciones"
    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String, nullable=False)
    fecha = Column(DateTime(timezone=True), nullable=False)          
    fecha_creacion = Column(DateTime(timezone=True), nullable=False) 
    estacion = Column(String, nullable=False)

class Modelo(Base):
    __tablename__ = "modelos"
    id = Column(Integer, primary_key=True, index=True)
    version = Column(String, nullable=False)
    fecha = Column(DateTime, nullable=False)
    archivo = Column(String, nullable=False) 
    txt_archivo = Column(String, nullable=False) 

class Attendance(Base):
    __tablename__ = "attendances"
    
    id = Column(Integer, primary_key=True, index=True)
    worker_name = Column(String, nullable=False)
    entry_date = Column(DateTime, nullable=False)
    exit_date = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.now)