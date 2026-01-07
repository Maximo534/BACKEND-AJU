const path = require('path');
const fs = require('fs');
const os = require('os');
//-----
var dstFileName = '';
var createdDate = '';
var updateDAte = '';
var userPc = '';

const isFile = fileName => {
    return fs.lstatSync(fileName).isFile()
}

const directoryPath = path.join(__dirname);

const getCurrenDate = () => {
    const hoy = new Date();
    const dia = String(hoy.getDate()).padStart(2, '0');
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const anio = hoy.getFullYear();
    return `${dia}/${mes}/${anio}`;
};

const getFileNames = (pathFoler,pathSrc) => {
    fs.readdirSync(pathFoler).forEach(function (el) {
        const pathElement = path.join(pathFoler, el);
        if(isFile(pathElement)){
            const ext = path.extname(el);
            if(ext){
                fs.appendFileSync(dstFileName,`\r\n"${pathSrc}/${el}"; ${ext}; ${createdDate}; ${updateDAte}; ${userPc}`);
            }
        }
        else{//dir
            getFileNames(pathElement,`${pathSrc}/${el}`);
        }
    });
}
//getFileNames(directoryPath,'');
const deleteFileDst = () =>{
    if (fs.existsSync(dstFileName)) {
        fs.unlinkSync(dstFileName);
    }
}

const isParamSigle = (val) =>{
    return val.startsWith('-');
}

const isParamWithValue = (val, index, array) =>{
    return val.startsWith('-') && index < array.length -1 && !array[index+1].trim().startsWith('-');
}

const processParams = (val, index, array) =>{
    if (isParamSigle(val)) {
        switch (val) {
            case '--file':
                if(!isParamWithValue(val, index, array)){
                    throw new Error(`El parametro ${val} requiere un valor`);
                }
                dstFileName = `${array[index+1].trim()}.csv`;
                break;
            case '-f':
                if(!isParamWithValue(val, index, array)){
                    throw new Error(`El parametro ${val} requiere un valor`);
                }
                dstFileName = `${array[index+1].trim()}.csv`;
                break;
            case '--create':
                if(!isParamWithValue(val, index, array)){
                    throw new Error(`El parametro ${val} requiere un valor`);
                }
                createdDate = `${array[index+1].trim()}`;
                break;
            case '-c':
                if(!isParamWithValue(val, index, array)){
                    throw new Error(`El parametro ${val} requiere un valor`);
                }
                createdDate = `${array[index+1].trim()}`;
                break;
            case '--update':
                if(!isParamWithValue(val, index, array)){
                    throw new Error(`El parametro ${val} requiere un valor`);
                }
                updateDAte = `${array[index+1].trim()}`;
                break;
            case '-u':
                if(!isParamWithValue(val, index, array)){
                    throw new Error(`El parametro ${val} requiere un valor`);
                }
                updateDAte = `${array[index+1].trim()}`;
                break;
            case '--user':
                if(!isParamWithValue(val, index, array)){
                    throw new Error(`El parametro ${val} requiere un valor`);
                }
                userPc = `${array[index+1].trim()}`;
                break;
            case '-us':
                if(!isParamWithValue(val, index, array)){
                    throw new Error(`El parametro ${val} requiere un valor`);
                }
                userPc = `${array[index+1].trim()}`;
                break;
            default:
                console.log("El parametro "+ comand +" no es valido");
                break;
        }
    }
}

process.argv.forEach(function (val, index, array) {
    //console.log(index + ': ' + val);
    if(index>=2){
        processParams(val.trim(), index, array)
    }
    if(index == array.length-1){
        if(dstFileName.trim() == ''){
            dstFileName = 'data.csv';
        }
        if(createdDate.trim() == ''){
            createdDate = getCurrenDate();
        }
        if(updateDAte.trim() == ''){
            updateDAte = getCurrenDate();
        }
        if(userPc.trim() == ''){
            userPc = os.userInfo().username;
        }
        deleteFileDst();
        getFileNames(directoryPath,'');
    }
    
});

/**
 * # sin parametros
 * node GenerarArchivosSP.js 
 * # con parametros
 * node GenerarArchivosSP.js -f data -c 02/03/2025 -u 02/03/2025 -us dpaucar
 */


