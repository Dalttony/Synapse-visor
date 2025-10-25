import os
import shutil
from datetime import datetime
from fastapi.responses import RedirectResponse, JSONResponse, HTMLResponse
from fastapi import Path
from fastapi import FastAPI, Request, Form, UploadFile, File, Depends, HTTPException
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session
from .database import Base, engine, SessionLocal
from .models import Estacion, Modelo, Attendance  # ← IMPORTANTE: Agrega Attendance aquí
from pydantic import BaseModel
from typing import Optional

# Importar schemas y crud
from . import schemas, crud



# Crear tablas (si no existen)
Base.metadata.create_all(bind=engine)

app = FastAPI(title="SIOMA Dashboard")
# carpeta uploads absoluta
BASE_DIR = os.path.dirname(__file__)
UPLOADS_DIR = os.path.join(BASE_DIR, "uploads")

# crear carpeta si no existe
os.makedirs(UPLOADS_DIR, exist_ok=True)

# ahora sí montamos la carpeta uploads
app.mount("/uploads", StaticFiles(directory=UPLOADS_DIR), name="uploads")
# carpeta base del módulo (esto asegura que templates/static se resuelvan correctamente)
BASE_DIR = os.path.dirname(__file__)
TEMPLATES_DIR = os.path.join(BASE_DIR, "templates")
STATIC_DIR = os.path.join(BASE_DIR, "static")
UPLOADS_DIR = os.path.join(BASE_DIR, "uploads")

os.makedirs(UPLOADS_DIR, exist_ok=True)

# montar static con ruta absoluta
app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")
templates = Jinja2Templates(directory=TEMPLATES_DIR)

# dependencia para obtener sesión de DB
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@app.get("/", response_class=HTMLResponse)
def home(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})


# ========================================
# NUEVO ENDPOINT PARA ANDROID STUDIO
# ========================================
@app.post("/api/recognitions")
async def create_recognition(
    recognition: schemas.RecognitionCreate,
    db: Session = Depends(get_db)
):
    """
    Endpoint para recibir datos de asistencia desde Android Studio
    Recibe: {id, payloadJson (como string), createdAt}
    """
    try:
      
        payload_data = crud.parse_payload_json(recognition.payloadJson)
        
       
        attendance = schemas.AttendanceCreate(
            workerName=payload_data.workerName,
            entryDate=payload_data.entryDate,
            exitDate=payload_data.exitDate
        )
        
       
        stored = crud.create_attendance(db, attendance)
        
        return {
            "success": True,
            "message": "Asistencia registrada exitosamente",
            "data": {
                "id": stored.id,
                "workerName": stored.worker_name,
                "entryDate": stored.entry_date.isoformat(),
                "exitDate": stored.exit_date.isoformat() if stored.exit_date else None
            }
        }
        
    except ValueError as e:
        raise HTTPException(status_code=400, detail=f"Error en el formato del JSON: {str(e)}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al procesar la solicitud: {str(e)}")

# ========================================
# FIN DEL NUEVO ENDPOINT
# ========================================
@app.get("/asistencias")
def ver_asistencias(request: Request, db: Session = Depends(get_db)):
    """Página para ver todas las asistencias registradas"""
    try:
        asistencias = db.query(Attendance).order_by(Attendance.id.desc()).all()
    except Exception as e:
        print(f"Error al cargar asistencias: {e}")
        asistencias = []
    
    return templates.TemplateResponse("asistencias.html", {
        "request": request, 
        "asistencias": asistencias
    })

@app.get("/api/asistencias")
def listar_asistencias_json(db: Session = Depends(get_db)):
    """Endpoint JSON para obtener todas las asistencias"""
    try:
        asistencias = db.query(Attendance).order_by(Attendance.id.desc()).all()
        resultado = [
            {
                "id": a.id,
                "workerName": a.worker_name,
                "entryDate": a.entry_date.isoformat() if a.entry_date else None,
                "exitDate": a.exit_date.isoformat() if a.exit_date else None,
                "createdAt": a.created_at.isoformat() if a.created_at else None
            }
            for a in asistencias
        ]
        return {"success": True, "data": resultado}
    except Exception as e:
        return {"success": False, "error": str(e)}


# -------- Modulo Modelos ----------
@app.get("/modelos")
def ver_modelos(request: Request, db: Session = Depends(get_db)):
    modelos = db.query(Modelo).order_by(Modelo.id.desc()).all()
    return templates.TemplateResponse("modelos.html", {"request": request, "modelos": modelos})

@app.post("/modelos")
async def crear_modelo(
    version: str = Form(...),
    fecha: str = Form(...),
    modelo_file: UploadFile = File(...),
    txt_file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    import os, shutil
    from datetime import datetime

    # carpeta uploads absoluta
    UPLOADS_DIR = os.path.join(os.path.dirname(__file__), "uploads")
    os.makedirs(UPLOADS_DIR, exist_ok=True)

    # Guardar archivo del modelo
    modelo_filename = modelo_file.filename
    modelo_path = os.path.join(UPLOADS_DIR, modelo_filename)
    if os.path.exists(modelo_path):
        name, ext = os.path.splitext(modelo_filename)
        timestamp = datetime.utcnow().strftime("%Y%m%d%H%M%S")
        modelo_filename = f"{name}_{timestamp}{ext}"
        modelo_path = os.path.join(UPLOADS_DIR, modelo_filename)
    with open(modelo_path, "wb") as buffer:
        shutil.copyfileobj(modelo_file.file, buffer)

    # Guardar archivo txt
    txt_filename = txt_file.filename
    txt_path = os.path.join(UPLOADS_DIR, txt_filename)
    if os.path.exists(txt_path):
        name, ext = os.path.splitext(txt_filename)
        timestamp = datetime.utcnow().strftime("%Y%m%d%H%M%S")
        txt_filename = f"{name}_{timestamp}{ext}"
        txt_path = os.path.join(UPLOADS_DIR, txt_filename)
    with open(txt_path, "wb") as buffer:
        shutil.copyfileobj(txt_file.file, buffer)

    # Convertir fecha a datetime
    try:
        dt_fecha = datetime.strptime(fecha, "%Y-%m-%d %H:%M:%S")
    except ValueError:
        return {"error": "Formato de fecha inválido. Debe ser YYYY-MM-DD HH:MM:SS"}

    # Guardar en la base de datos
    nuevo = Modelo(
        version=version,
        fecha=dt_fecha,
        archivo=modelo_path,
        txt_archivo=txt_path  # asegúrate de tener esta columna en tu tabla
    )
    db.add(nuevo)
    db.commit()
    db.refresh(nuevo)

    return RedirectResponse(url="/modelos", status_code=303)

# -------- Endpoints de consulta que pediste ----------
@app.get("/modelos/ultimo")
def obtener_ultimo_modelo(db: Session = Depends(get_db)):
    modelo = db.query(Modelo).order_by(Modelo.id.desc()).first()
    if not modelo:
        return JSONResponse(status_code=404, content={"mensaje": "No hay modelos registrados"})
    return {
        "id": modelo.id,
        "version": modelo.version,
        "fecha": modelo.fecha.isoformat(),
        "archivo": modelo.archivo,
        "txt_archivo": modelo.txt_archivo
    }

@app.get("/modelos/{modelo_id}")
def obtener_modelo(modelo_id: int, db: Session = Depends(get_db)):
    modelo = db.query(Modelo).filter(Modelo.id == modelo_id).first()
    if not modelo:
        return JSONResponse(status_code=404, content={"mensaje": f"No se encontró un modelo con ID {modelo_id}"})
    return {
        "id": modelo.id,
        "version": modelo.version,
        "fecha": modelo.fecha.isoformat(),
        "archivo": modelo.archivo,
        "txt_archivo": modelo.txt_archivo
    }

@app.post("/modelos/{modelo_id}/eliminar")
def eliminar_modelo(modelo_id: int = Path(...), db: Session = Depends(get_db)):
    modelo = db.query(Modelo).filter(Modelo.id == modelo_id).first()
    if not modelo:
        return JSONResponse(status_code=404, content={"error": f"No existe un modelo con id {modelo_id}"})
    
    # eliminar archivo físico si quieres
    import os
    if modelo.archivo and os.path.exists(modelo.archivo):
        os.remove(modelo.archivo)
    
    db.delete(modelo)
    db.commit()
    
    # redirigir al dashboard de modelos
    return RedirectResponse(url="/modelos", status_code=303)